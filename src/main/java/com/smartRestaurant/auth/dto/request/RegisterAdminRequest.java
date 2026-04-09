package com.smartRestaurant.auth.dto.request;

import com.smartRestaurant.auth.model.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAdminRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @Email(message = "El formato del email no es válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotNull(message = "El rol es obligatorio")
    private UserRole role;
}
