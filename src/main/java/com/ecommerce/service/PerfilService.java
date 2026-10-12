package com.ecommerce.service;

import com.ecommerce.dto.ActualizarPerfilRequest;
import com.ecommerce.dto.CambiarPasswordRequest;
import com.ecommerce.dto.UsuarioResponse;
import com.ecommerce.entity.Cliente;
import com.ecommerce.entity.Usuario;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ClienteRepository;
import com.ecommerce.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService {

    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final PasswordEncoder encoder;

    public PerfilService(UsuarioRepository usuarios, ClienteRepository clientes, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(String email) {
        Usuario u = user(email);
        Cliente c = clientes.findByUsuarioId(u.getId()).orElse(null);
        return new UsuarioResponse(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getRol().name(),
                c != null && c.getDireccion() != null ? c.getDireccion() : "",
                c != null && c.getTelefono() != null ? c.getTelefono() : "",
                u.getFechaRegistro()
        );
    }

    @Transactional
    public UsuarioResponse actualizar(String email, ActualizarPerfilRequest r) {
        Usuario u = user(email);
        u.setNombre(r.getNombre().trim());

        Cliente c = clientes.findByUsuarioId(u.getId()).orElseGet(() -> {
            Cliente nuevo = new Cliente();
            nuevo.setUsuario(u);
            return nuevo;
        });

        c.setDireccion(r.getDireccion().trim());
        c.setTelefono(r.getTelefono().trim());

        clientes.save(c);
        usuarios.save(u);

        return obtener(email);
    }

    @Transactional
    public void cambiarPassword(String email, CambiarPasswordRequest r) {
        Usuario u = user(email);

        if (!encoder.matches(r.getPasswordActual(), u.getPassword())) {
            throw new BadRequestException("La contraseña actual ingresada es incorrecta");
        }

        if (encoder.matches(r.getPasswordNueva(), u.getPassword())) {
            throw new BadRequestException("La nueva contraseña debe ser diferente a la contraseña actual");
        }

        u.setPassword(encoder.encode(r.getPasswordNueva()));
        usuarios.save(u);
    }

    private Usuario user(String email) {
        return usuarios.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }
}
