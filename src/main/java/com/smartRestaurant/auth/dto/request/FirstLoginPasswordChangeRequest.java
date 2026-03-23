package com.smartRestaurant.auth.dto.request;

import com.smartRestaurant.auth.validation.Password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cambio de contraseña obligatorio en primer login
 * No requiere contraseña actual ni OTP (ya validado en 2FA)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirstLoginPasswordChangeRequest {

    @Email(message = "El formato del email no es válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Password
    private String newPassword;

    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmPassword;
}
