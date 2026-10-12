package com.ecommerce.dto;
import jakarta.validation.constraints.NotBlank; import lombok.Getter; import lombok.Setter;
@Getter @Setter public class CambiarEstadoPagoRequest { @NotBlank(message="El estado de pago es obligatorio") private String estado; }
