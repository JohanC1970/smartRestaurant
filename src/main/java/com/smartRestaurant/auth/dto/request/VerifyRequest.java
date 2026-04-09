package com.smartRestaurant.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyRequest {

    @Email(message = "El formato del email no es válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "El código OTP es obligatorio")
    private String code;
}
