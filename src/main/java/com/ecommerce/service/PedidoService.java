package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.*;
import com.ecommerce.enums.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidos;
    private final CarritoRepository carritos;
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final InventarioService inventario;

    public PedidoService(PedidoRepository pedidos, CarritoRepository carritos,
                         UsuarioRepository usuarios, ClienteRepository clientes,
                         InventarioService inventario) {
        this.pedidos = pedidos;
        this.carritos = carritos;
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.inventario = inventario;
    }

    @Transactional
    public PedidoResponse confirmarPedido(String email) {
        Usuario u = user(email);
        Cliente c = clientes.findByUsuarioId(u.getId())
                .orElseThrow(() -> new BadRequestException("Debe completar su dirección y teléfono en su perfil antes de comprar"));

        if (c.getDireccion() == null || c.getDireccion().trim().isEmpty() ||
            c.getTelefono() == null || c.getTelefono().trim().isEmpty()) {
            throw new BadRequestException("La dirección y el teléfono son obligatorios para generar el pedido");
        }

        Carrito cart = carritos.findByUsuarioId(u.getId())
                .orElseThrow(() -> new BadRequestException("El carrito no existe"));

        if (cart.getDetalles().isEmpty()) {
            throw new BadRequestException("El carrito está vacío");
        }

        Pedido p = new Pedido();
        p.setUsuario(u);
        p.setDireccionEnvio(c.getDireccion().trim());
        p.setTelefonoContacto(c.getTelefono().trim());
        p.setEstado(EstadoPedido.PENDIENTE);
        p.setEstadoPago(EstadoPago.PENDIENTE);
        p.setTotal(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCarrito dc : cart.getDetalles()) {
            DetallePedido d = new DetallePedido();
            d.setPedido(p);
            d.setProducto(dc.getProducto());
            d.setCantidad(dc.getCantidad());
            d.setPrecioUnitario(dc.getPrecioUnitario());
            BigDecimal subtotal = dc.getPrecioUnitario().multiply(BigDecimal.valueOf(dc.getCantidad()));
            d.setSubtotal(subtotal);
            p.getDetalles().add(d);
            total = total.add(subtotal);
        }

        p.setTotal(total);
        p = pedidos.save(p);

        cart.getDetalles().clear();
        carritos.save(cart);

        return map(p);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosUsuario(String email) {
        return pedidos.findByUsuarioIdOrderByFechaPedidoDesc(user(email).getId())
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedidoUsuario(String email, Long id) {
        Pedido p = pedidos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        if (!p.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new UnauthorizedException("No tiene permisos para consultar este pedido");
        }
        return map(p);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodosLosPedidos() {
        return pedidos.findAllByOrderByFechaPedidoDesc()
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedido(Long id) {
        return map(pedidos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id)));
    }

    @Transactional
    public PedidoResponse registrarPago(String email, Long id, PagoRequest r) {
        Pedido p = owned(email, id);
        if (p.getEstadoPago() == EstadoPago.CONFIRMADO) {
            throw new BadRequestException("El pago ya fue confirmado previamente");
        }
        p.setMetodoPago(r.getMetodoPago());
        if (r.getReferencia() != null && !r.getReferencia().trim().isEmpty()) {
            p.setReferenciaPago(r.getReferencia().trim());
        }
        p.setEstadoPago(EstadoPago.PENDIENTE);
        return map(pedidos.save(p));
    }

    @Transactional
    public PedidoResponse confirmarPago(Long id) {
        Pedido p = find(id);
        if (p.getMetodoPago() == null) {
            throw new BadRequestException("El cliente debe seleccionar un método de pago antes de confirmar");
        }
        if (p.getEstado() == EstadoPedido.CANCELADO || p.getEstado() == EstadoPedido.ENTREGADO) {
            throw new BadRequestException("El pedido no puede recibir pago en su estado actual: " + p.getEstado());
        }
        if (p.getEstadoPago() == EstadoPago.CONFIRMADO) {
            throw new BadRequestException("El pago de este pedido ya fue confirmado");
        }
        p.setEstadoPago(EstadoPago.CONFIRMADO);
        p.setFechaPago(LocalDateTime.now());
        p.setEstado(EstadoPedido.PROCESANDO);

        for (DetallePedido d : p.getDetalles()) {
            inventario.descontarStock(d.getProducto().getId(), d.getCantidad());
        }

        return map(pedidos.save(p));
    }

    @Transactional
    public PedidoResponse rechazarPago(Long id) {
        Pedido p = find(id);
        if (p.getEstadoPago() == EstadoPago.CONFIRMADO) {
            throw new BadRequestException("No se puede rechazar un pago que ya fue confirmado");
        }
        p.setEstadoPago(EstadoPago.RECHAZADO);
        return map(pedidos.save(p));
    }

    @Transactional
    public PedidoResponse confirmarEntrega(Long id) {
        Pedido p = find(id);
        if (p.getEstadoPago() != EstadoPago.CONFIRMADO) {
            throw new BadRequestException("No se puede confirmar la entrega porque el pago aún no ha sido confirmado");
        }
        if (p.getEstado() == EstadoPedido.CANCELADO) {
            throw new BadRequestException("No se puede entregar un pedido cancelado");
        }
        p.setEstado(EstadoPedido.ENTREGADO);
        p.setFechaEntregaConfirmada(LocalDateTime.now());
        return map(pedidos.save(p));
    }

    @Transactional
    public PedidoResponse actualizarEstado(Long id, ActualizarEstadoPedidoRequest r) {
        Pedido p = find(id);
        EstadoPedido e;
        try {
            e = EstadoPedido.valueOf(r.getEstado().trim().toUpperCase());
        } catch (Exception ex) {
            throw new BadRequestException("Estado de pedido inválido: " + r.getEstado());
        }
        if (e == EstadoPedido.ENTREGADO) {
            return confirmarEntrega(id);
        }
        p.setEstado(e);
        return map(pedidos.save(p));
    }

    private Pedido owned(String email, Long id) {
        Pedido p = find(id);
        if (!p.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new UnauthorizedException("No puede modificar este pedido");
        }
        return p;
    }

    private Pedido find(Long id) {
        return pedidos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));
    }

    private Usuario user(String email) {
        return usuarios.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    private PedidoResponse map(Pedido p) {
        List<DetallePedidoResponse> ds = p.getDetalles().stream().map(d -> {
            DetallePedidoResponse x = new DetallePedidoResponse();
            x.setId(d.getId());
            x.setProductoNombre(d.getProducto() != null ? d.getProducto().getNombre() : "Producto");
            x.setCantidad(d.getCantidad());
            x.setPrecioUnitario(d.getPrecioUnitario());
            x.setSubtotal(d.getSubtotal());
            return x;
        }).collect(Collectors.toList());

        return new PedidoResponse(
                p.getId(),
                p.getFechaPedido(),
                p.getTotal(),
                p.getEstado().name(),
                p.getDireccionEnvio(),
                p.getTelefonoContacto(),
                p.getUsuario().getNombre(),
                p.getUsuario().getEmail(),
                p.getEstadoPago().name(),
                p.getMetodoPago() == null ? null : p.getMetodoPago().name(),
                p.getReferenciaPago(),
                p.getFechaPago(),
                p.getFechaEntregaConfirmada(),
                ds
        );
    }
}
