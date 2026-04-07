package com.smartRestaurant.auth.dto.request;

import com.smartRestaurant.auth.validation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(
        regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s]+$",
        message = "El nombre solo puede contener letras y espacios"
    )
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Pattern(
        regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ\\s]+$",
        message = "El apellido solo puede contener letras y espacios"
    )
    private String lastName;

    @Email(message = "El formato del email no es válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Password(message = "La contraseña debe tener al menos 6 caracteres, una mayúscula, una minúscula y un carácter especial (@#$...)")
    private String password;

    private com.smartRestaurant.auth.model.enums.UserRole role;

    /**
     * ID del restaurante al que pertenece el usuario.
     * Obligatorio para COCINA y MESERO.
     * Para RESTAURANTE, se genera automáticamente.
     */
    private Long restaurantId;
}
