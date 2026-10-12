package com.ecommerce.dto;
import java.time.LocalDateTime;
public record UsuarioResponse(Long id,String nombre,String email,String rol,String direccion,String telefono,LocalDateTime fechaRegistro) {}
