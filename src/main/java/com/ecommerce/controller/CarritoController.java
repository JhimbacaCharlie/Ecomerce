package com.ecommerce.controller;

import com.ecommerce.dto.ActualizarCantidadRequest;
import com.ecommerce.dto.CarritoItemRequest;
import com.ecommerce.dto.CarritoResponse;
import com.ecommerce.service.CarritoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public ResponseEntity<CarritoResponse> obtenerCarrito(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(carritoService.obtenerCarrito(userDetails.getUsername()));
    }

    @PostMapping("/items")
    public ResponseEntity<CarritoResponse> agregarItem(@AuthenticationPrincipal UserDetails userDetails,
                                                       @Valid @RequestBody CarritoItemRequest request) {
        return ResponseEntity.ok(carritoService.agregarItem(userDetails.getUsername(), request));
    }

    @PutMapping("/items/{detalleId}")
    public ResponseEntity<CarritoResponse> actualizarCantidad(@AuthenticationPrincipal UserDetails userDetails,
                                                              @PathVariable Long detalleId,
                                                              @Valid @RequestBody ActualizarCantidadRequest request) {
        return ResponseEntity.ok(carritoService.actualizarCantidad(userDetails.getUsername(), detalleId, request));
    }

    @DeleteMapping("/items/{detalleId}")
    public ResponseEntity<CarritoResponse> eliminarItem(@AuthenticationPrincipal UserDetails userDetails,
                                                        @PathVariable Long detalleId) {
        return ResponseEntity.ok(carritoService.eliminarItem(userDetails.getUsername(), detalleId));
    }

    @DeleteMapping
    public ResponseEntity<CarritoResponse> vaciarCarrito(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(carritoService.vaciarCarrito(userDetails.getUsername()));
    }
}