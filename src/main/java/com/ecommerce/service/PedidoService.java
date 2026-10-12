package com.ecommerce.service;

import com.ecommerce.dto.CategoriaResponse;
import com.ecommerce.dto.ProductoRequest;
import com.ecommerce.dto.ProductoResponse;
import com.ecommerce.entity.Categoria;
import com.ecommerce.entity.Producto;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoriaRepository;
import com.ecommerce.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setCostoUnitario(request.getCostoUnitario());
        producto.setStock(request.getStock());
        producto.setImagenUrl(request.getImagenUrl());

        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con id: " + request.getCategoriaId()));
            producto.setCategoria(categoria);
        }

        producto = productoRepository.save(producto);
        return convertirAProductoResponse(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return productoRepository.findAll().stream()
                .map(this::convertirAProductoResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        return convertirAProductoResponse(producto);
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setCostoUnitario(request.getCostoUnitario());
        producto.setStock(request.getStock());
        producto.setImagenUrl(request.getImagenUrl());

        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con id: " + request.getCategoriaId()));
            producto.setCategoria(categoria);
        } else {
            producto.setCategoria(null);
        }

        producto = productoRepository.save(producto);
        return convertirAProductoResponse(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        productoRepository.delete(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> buscar(String query) {
        return productoRepository.buscarPorNombre(query).stream()
                .map(this::convertirAProductoResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> filtrarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId).stream()
                .map(this::convertirAProductoResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(c -> new CategoriaResponse(c.getId(), c.getNombre()))
                .collect(Collectors.toList());
    }

    private ProductoResponse convertirAProductoResponse(Producto producto) {
        ProductoResponse response = new ProductoResponse();
        response.setId(producto.getId());
        response.setNombre(producto.getNombre());
        response.setDescripcion(producto.getDescripcion());
        response.setPrecio(producto.getPrecio());
        response.setCostoUnitario(producto.getCostoUnitario());
        response.setStock(producto.getStock());
        response.setImagenUrl(producto.getImagenUrl());
        if (producto.getCategoria() != null) {
            response.setCategoriaNombre(producto.getCategoria().getNombre());
            response.setCategoriaId(producto.getCategoria().getId());
        }
        return response;
    }
}
