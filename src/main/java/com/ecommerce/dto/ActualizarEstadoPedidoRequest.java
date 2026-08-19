package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarEstadoPedidoRequest {

    @NotBlank(message = "El estado es obligatorio")
    private String estado;
}