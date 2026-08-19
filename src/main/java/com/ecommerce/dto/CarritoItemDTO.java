package com.ecommerce.dto;

import lombok.*;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class CarritoItemDTO {
    private Long detalleId;
    private Long productoId;
    private String nombreProducto;
    private Double precioUnitario;
    private Integer cantidad;
    private Double subtotal;
}