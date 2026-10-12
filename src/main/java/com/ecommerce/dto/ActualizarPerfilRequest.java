package com.ecommerce.dto;
import jakarta.validation.constraints.*; import lombok.Getter; import lombok.Setter;
@Getter @Setter public class ActualizarPerfilRequest { @NotBlank @Size(min=2,max=80) private String nombre; @NotBlank @Size(min=5,max=180) private String direccion; @NotBlank @Pattern(regexp="^[0-9+ -]{7,20}$") private String telefono; }
