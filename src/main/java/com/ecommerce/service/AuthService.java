package com.ecommerce.service;

import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.RegistroRequest;
import com.ecommerce.entity.Cliente;
import com.ecommerce.entity.Usuario;
import com.ecommerce.enums.Rol;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.repository.ClienteRepository;
import com.ecommerce.repository.UsuarioRepository;
import com.ecommerce.security.JwtService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthService(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       UserDetailsService userDetailsService) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya esta registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(Rol.CLIENTE);
        usuario = usuarioRepository.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setDireccion(request.getDireccion());
        cliente.setTelefono(request.getTelefono());
        clienteRepository.save(cliente);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtService.generarToken(userDetails);

        return new AuthResponse(token, usuario.getEmail(), usuario.getNombre(), usuario.getRol().name());
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Credenciales invalidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadRequestException("Credenciales invalidas");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtService.generarToken(userDetails);

        return new AuthResponse(token, usuario.getEmail(), usuario.getNombre(), usuario.getRol().name());
    }
}