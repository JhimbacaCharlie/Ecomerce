package com.ecommerce.dto;
import com.ecommerce.enums.MetodoPago; import jakarta.validation.constraints.NotNull; import jakarta.validation.constraints.Size; import lombok.Getter; import lombok.Setter;
@Getter @Setter public class PagoRequest { @NotNull(message="El metodo de pago es obligatorio") private MetodoPago metodoPago; @Size(max=100,message="La referencia es demasiado larga") private String referencia; }
