package com.smartRestaurant.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String message;
    private boolean is2faRequired;

    /**
     * RF-02: Indica si el usuario debe cambiar su contraseña
     * Se usa para empleados registrados por administradores con contraseñas
     * temporales
     */
    @Builder.Default
    private boolean requiresPasswordChange = false;
}
