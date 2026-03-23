package com.smartRestaurant.auth.dto.request;

import com.smartRestaurant.auth.model.enums.SocialProvider;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de login/registro con proveedor social
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {

    @NotNull(message = "El proveedor es obligatorio")
    private SocialProvider provider;

    @NotBlank(message = "El token de acceso es obligatorio")
    private String accessToken;

    // Campos opcionales que pueden venir del frontend
    @Email
    private String email;

    private String firstName;
    private String lastName;
    private String profilePicture;
}
