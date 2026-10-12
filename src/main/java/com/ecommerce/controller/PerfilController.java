package com.ecommerce.controller;

import com.ecommerce.dto.ActualizarPerfilRequest;
import com.ecommerce.dto.CambiarPasswordRequest;
import com.ecommerce.dto.UsuarioResponse;
import com.ecommerce.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public ResponseEntity<UsuarioResponse> obtenerPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(perfilService.obtener(userDetails.getUsername()));
    }

    @PutMapping
    public ResponseEntity<UsuarioResponse> actualizarPerfil(@AuthenticationPrincipal UserDetails userDetails,
                                                            @Valid @RequestBody ActualizarPerfilRequest request) {
        return ResponseEntity.ok(perfilService.actualizar(userDetails.getUsername(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> cambiarPassword(@AuthenticationPrincipal UserDetails userDetails,
                                                @Valid @RequestBody CambiarPasswordRequest request) {
        perfilService.cambiarPassword(userDetails.getUsername(), request);
        return ResponseEntity.noContent().build();
    }
}
