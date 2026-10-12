package com.ecommerce.controller;

import com.ecommerce.dto.PagoRequest;
import com.ecommerce.dto.PedidoResponse;
import com.ecommerce.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PedidoService pedidoService;

    public PagoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /**
     * Registra el pago por parte del cliente (método de pago y referencia/código de operación).
     * El pedido queda en estadoPago = PENDIENTE a la espera de confirmación por el administrador.
     */
    @PostMapping("/pedidos/{id}")
    public ResponseEntity<PedidoResponse> pagar(@AuthenticationPrincipal UserDetails userDetails,
                                                @PathVariable Long id,
                                                @Valid @RequestBody PagoRequest request) {
        return ResponseEntity.ok(pedidoService.registrarPago(userDetails.getUsername(), id, request));
    }

    /**
     * Confirmación exclusiva para rol ADMIN:
     * Valida el pago, descuenta el stock de inventario de forma sincronizada y pasa el pedido a PROCESANDO.
     */
    @PutMapping("/pedidos/{id}/confirmar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoResponse> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.confirmarPago(id));
    }

    /**
     * Rechazo exclusivo para rol ADMIN:
     * Marca el estado del pago como RECHAZADO (por ejemplo si el voucher no coincide).
     */
    @PutMapping("/pedidos/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoResponse> rechazar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.rechazarPago(id));
    }

    /**
     * Confirmación de entrega exclusiva para rol ADMIN:
     * Marca el pedido como ENTREGADO y registra la fecha de entrega.
     */
    @PutMapping("/pedidos/{id}/entrega")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoResponse> entrega(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.confirmarEntrega(id));
    }
}
