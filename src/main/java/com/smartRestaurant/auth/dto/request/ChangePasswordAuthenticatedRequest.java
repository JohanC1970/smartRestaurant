package com.smartRestaurant.auth.dto.request;

import com.smartRestaurant.auth.validation.Password;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cambio de contraseña cuando el usuario ya está autenticado.
 * No requiere OTP — el JWT activo es suficiente verificación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordAuthenticatedRequest {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Password
    private String newPassword;
}
