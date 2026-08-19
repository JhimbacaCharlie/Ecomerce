package com.ecommerce.dto;
import lombok.*;
import java.time.LocalDateTime;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class ProductoDTO {
    private Long id;
    private String nombre;
    private String categoria;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private String imagenUrl;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}