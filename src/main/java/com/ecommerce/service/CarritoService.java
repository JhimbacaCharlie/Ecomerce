package com.ecommerce.service;

import com.ecommerce.dto.ActualizarCantidadRequest;
import com.ecommerce.dto.CarritoItemRequest;
import com.ecommerce.dto.CarritoItemResponse;
import com.ecommerce.dto.CarritoResponse;
import com.ecommerce.entity.Carrito;
import com.ecommerce.entity.DetalleCarrito;
import com.ecommerce.entity.Producto;
import com.ecommerce.entity.Usuario;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CarritoRepository;
import com.ecommerce.repository.ProductoRepository;
import com.ecommerce.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public CarritoService(CarritoRepository carritoRepository, ProductoRepository productoRepository,
                          UsuarioRepository usuarioRepository) {
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public CarritoResponse obtenerCarrito(String email) {
        Usuario usuario = obtenerUsuario(email);
        Carrito carrito = obtenerOCrearCarrito(usuario);
        return convertirACarritoResponse(carrito);
    }

    @Transactional
    public CarritoResponse agregarItem(String email, CarritoItemRequest request) {
        Usuario usuario = obtenerUsuario(email);
        Carrito carrito = obtenerOCrearCarrito(usuario);
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + request.getProductoId()));

        if (producto.getStock() < request.getCantidad()) {
            throw new BadRequestException("Stock insuficiente. Stock disponible: " + producto.getStock());
        }

        boolean encontrado = false;
        for (DetalleCarrito detalle : carrito.getDetalles()) {
            if (detalle.getProducto().getId().equals(producto.getId())) {
                detalle.setCantidad(detalle.getCantidad() + request.getCantidad());
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            DetalleCarrito nuevoDetalle = new DetalleCarrito();
            nuevoDetalle.setCarrito(carrito);
            nuevoDetalle.setProducto(producto);
            nuevoDetalle.setCantidad(request.getCantidad());
            nuevoDetalle.setPrecioUnitario(producto.getPrecio());
            carrito.getDetalles().add(nuevoDetalle);
        }

        carritoRepository.save(carrito);
        return convertirACarritoResponse(carrito);
    }

    @Transactional
    public CarritoResponse actualizarCantidad(String email, Long detalleId, ActualizarCantidadRequest request) {
        Usuario usuario = obtenerUsuario(email);
        Carrito carrito = obtenerOCrearCarrito(usuario);

        DetalleCarrito detalleActualizado = null;
        for (DetalleCarrito detalle : carrito.getDetalles()) {
            if (detalle.getId().equals(detalleId)) {
                if (detalle.getProducto().getStock() < request.getCantidad()) {
                    throw new BadRequestException("Stock insuficiente. Stock disponible: " + detalle.getProducto().getStock());
                }
                detalle.setCantidad(request.getCantidad());
                detalleActualizado = detalle;
                break;
            }
        }

        if (detalleActualizado == null) {
            throw new ResourceNotFoundException("Detalle no encontrado en el carrito con id: " + detalleId);
        }

        carritoRepository.save(carrito);
        return convertirACarritoResponse(carrito);
    }

    @Transactional
    public CarritoResponse eliminarItem(String email, Long detalleId) {
        Usuario usuario = obtenerUsuario(email);
        Carrito carrito = obtenerOCrearCarrito(usuario);

        boolean eliminado = false;
        for (int i = 0; i < carrito.getDetalles().size(); i++) {
            if (carrito.getDetalles().get(i).getId().equals(detalleId)) {
                carrito.getDetalles().remove(i);
                eliminado = true;
                break;
            }
        }

        if (!eliminado) {
            throw new ResourceNotFoundException("Detalle no encontrado en el carrito con id: " + detalleId);
        }

        carritoRepository.save(carrito);
        return convertirACarritoResponse(carrito);
    }

    @Transactional
    public CarritoResponse vaciarCarrito(String email) {
        Usuario usuario = obtenerUsuario(email);
        Carrito carrito = obtenerOCrearCarrito(usuario);
        carrito.getDetalles().clear();
        carritoRepository.save(carrito);
        return convertirACarritoResponse(carrito);
    }

    private Usuario obtenerUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

    private Carrito obtenerOCrearCarrito(Usuario usuario) {
        return carritoRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuario(usuario);
                    nuevoCarrito.setDetalles(new ArrayList<>());
                    return carritoRepository.save(nuevoCarrito);
                });
    }

    private CarritoResponse convertirACarritoResponse(Carrito carrito) {
        List<CarritoItemResponse> items = new ArrayList<>();
        int totalItems = 0;
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleCarrito detalle : carrito.getDetalles()) {
            CarritoItemResponse item = new CarritoItemResponse();
            item.setDetalleId(detalle.getId());
            item.setProductoId(detalle.getProducto().getId());
            item.setProductoNombre(detalle.getProducto().getNombre());
            item.setImagenUrl(detalle.getProducto().getImagenUrl());
            item.setCantidad(detalle.getCantidad());
            item.setPrecioUnitario(detalle.getPrecioUnitario());
            BigDecimal subtotal = detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad()));
            item.setSubtotal(subtotal);
            items.add(item);
            totalItems += detalle.getCantidad();
            total = total.add(subtotal);
        }

        return new CarritoResponse(carrito.getId(), items, totalItems, total);
    }
}