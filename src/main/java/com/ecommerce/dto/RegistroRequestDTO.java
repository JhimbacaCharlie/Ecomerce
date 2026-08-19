package com.ecommerce.dto;
import lombok.*;
@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class RegistroRequestDTO {
    private String username;
    private String password;
    private String nombres;
    private String correo;
    private String direccion;
    private String telefono;
}