package com.ecommerce.dto;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class PedidoDTO {
    private Long id;
    private LocalDateTime fecha;
    private Double total;
    private String estado;
    private List<CarritoItemDTO> detalles; // Reutilizamos el DTO para no crear uno nuevo
}