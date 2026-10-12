package com.ecommerce.dto;
import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.Size; import lombok.Getter; import lombok.Setter;
@Getter @Setter public class CambiarPasswordRequest { @NotBlank private String passwordActual; @NotBlank @Size(min=8,max=72) private String passwordNueva; }
