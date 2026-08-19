package com.ecommerce.service;

import com.ecommerce.dto.ActualizarEstadoPedidoRequest;
import com.ecommerce.dto.DetallePedidoResponse;
import com.ecommerce.dto.PedidoResponse;
import com.ecommerce.entity.Carrito;
import com.ecommerce.entity.DetalleCarrito;
import com.ecommerce.entity.DetallePedido;
import com.ecommerce.entity.Pedido;
import com.ecommerce.entity.Usuario;
import com.ecommerce.enums.EstadoPedido;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CarritoRepository;
import com.ecommerce.repository.PedidoRepository;
import com.ecommerce.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarritoRepository carritoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(PedidoRepository pedidoRepository, CarritoRepository carritoRepository,
                         UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.carritoRepository = carritoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public PedidoResponse confirmarPedido(String email) {
        Usuario usuario = obtenerUsuario(email);
        Carrito carrito = carritoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new BadRequestException("No existe un carrito para este usuario"));

        if (carrito.getDetalles().isEmpty()) {
            throw new BadRequestException("El carrito esta vacio");
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDetalles(new ArrayList<>());

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleCarrito dc : carrito.getDetalles()) {
            if (dc.getProducto().getStock() < dc.getCantidad()) {
                throw new BadRequestException("Stock insuficiente para el producto: " + dc.getProducto().getNombre());
            }

            dc.getProducto().setStock(dc.getProducto().getStock() - dc.getCantidad());

            DetallePedido dp = new DetallePedido();
            dp.setPedido(pedido);
            dp.setProducto(dc.getProducto());
            dp.setCantidad(dc.getCantidad());
            dp.setPrecioUnitario(dc.getPrecioUnitario());
            BigDecimal subtotal = dc.getPrecioUnitario().multiply(BigDecimal.valueOf(dc.getCantidad()));
            dp.setSubtotal(subtotal);
            pedido.getDetalles().add(dp);
            total = total.add(subtotal);
        }

        pedido.setTotal(total);
        pedidoRepository.save(pedido);

        carrito.getDetalles().clear();
        carritoRepository.save(carrito);

        return convertirAPedidoResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosUsuario(String email) {
        Usuario usuario = obtenerUsuario(email);
        return pedidoRepository.findByUsuarioIdOrderByFechaPedidoDesc(usuario.getId()).stream()
                .map(this::convertirAPedidoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedidoUsuario(String email, Long pedidoId) {
        Usuario usuario = obtenerUsuario(email);
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + pedidoId));

        if (!pedido.getUsuario().getId().equals(usuario.getId())) {
            throw new ResourceNotFoundException("Pedido no encontrado con id: " + pedidoId);
        }

        return convertirAPedidoResponse(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodosLosPedidos() {
        return pedidoRepository.findAll().stream()
                .map(this::convertirAPedidoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + pedidoId));
        return convertirAPedidoResponse(pedido);
    }

    @Transactional
    public PedidoResponse actualizarEstado(Long pedidoId, ActualizarEstadoPedidoRequest request) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + pedidoId));

        try {
            EstadoPedido nuevoEstado = EstadoPedido.valueOf(request.getEstado().toUpperCase());
            pedido.setEstado(nuevoEstado);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado invalido: " + request.getEstado());
        }

        pedido = pedidoRepository.save(pedido);
        return convertirAPedidoResponse(pedido);
    }

    private Usuario obtenerUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    private PedidoResponse convertirAPedidoResponse(Pedido pedido) {
        List<DetallePedidoResponse> detallesResp = new ArrayList<>();
        for (DetallePedido dp : pedido.getDetalles()) {
            DetallePedidoResponse dr = new DetallePedidoResponse();
            dr.setId(dp.getId());
            dr.setProductoNombre(dp.getProducto().getNombre());
            dr.setCantidad(dp.getCantidad());
            dr.setPrecioUnitario(dp.getPrecioUnitario());
            dr.setSubtotal(dp.getSubtotal());
            detallesResp.add(dr);
        }

        return new PedidoResponse(pedido.getId(), pedido.getFechaPedido(), pedido.getTotal(),
                pedido.getEstado().name(), pedido.getDireccionEnvio(), pedido.getUsuario().getNombre(), detallesResp);
    }
}