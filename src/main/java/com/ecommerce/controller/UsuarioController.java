package com.ecommerce.controller;

import com.ecommerce.dto.UsuarioResponse;
import com.ecommerce.entity.Cliente;
import com.ecommerce.entity.Usuario;
import com.ecommerce.repository.ClienteRepository;
import com.ecommerce.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;

    public UsuarioController(UsuarioRepository usuarios, ClienteRepository clientes) {
        this.usuarios = usuarios;
        this.clientes = clientes;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        List<UsuarioResponse> list = usuarios.findAll().stream().map(u -> {
            Cliente c = clientes.findByUsuarioId(u.getId()).orElse(null);
            return new UsuarioResponse(
                    u.getId(),
                    u.getNombre(),
                    u.getEmail(),
                    u.getRol().name(),
                    c != null && c.getDireccion() != null ? c.getDireccion() : "-",
                    c != null && c.getTelefono() != null ? c.getTelefono() : "-",
                    u.getFechaRegistro()
            );
        }).toList();

        return ResponseEntity.ok(list);
    }
}
