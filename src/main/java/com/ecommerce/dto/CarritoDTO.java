package com.ecommerce.dto;
import lombok.*;
import java.util.List;
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class CarritoDTO {
    private Long carritoId;
    private List<CarritoItemDTO> items;
    private Double total;
}