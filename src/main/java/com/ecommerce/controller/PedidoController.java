package com.ecommerce.controller;

import com.ecommerce.dto.ActualizarEstadoPedidoRequest;
import com.ecommerce.dto.PedidoResponse;
import com.ecommerce.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping("/confirmar")
    public ResponseEntity<PedidoResponse> confirmarPedido(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pedidoService.confirmarPedido(userDetails.getUsername()));
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<PedidoResponse>> listarMisPedidos(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(pedidoService.listarPedidosUsuario(userDetails.getUsername()));
    }

    @GetMapping("/mis-pedidos/{id}")
    public ResponseEntity<PedidoResponse> obtenerMiPedido(@AuthenticationPrincipal UserDetails userDetails,
                                                          @PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPedidoUsuario(userDetails.getUsername(), id));
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PedidoResponse>> listarTodosLosPedidos() {
        return ResponseEntity.ok(pedidoService.listarTodosLosPedidos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoResponse> obtenerPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPedido(id));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoResponse> actualizarEstado(@PathVariable Long id,
                                                            @Valid @RequestBody ActualizarEstadoPedidoRequest request) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, request));
    }
}