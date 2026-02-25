package com.smartRestaurant.auth.dto.request;

import com.smartRestaurant.auth.validation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @Email(message = "El formato del email no es válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Password(message = "La contraseña debe tener al menos 6 caracteres, una mayúscula, una minúscula y un carácter especial (@#$...)")
    private String password;

    private com.smartRestaurant.auth.model.enums.UserRole role;
}
