package com.smartRestaurant.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

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

}
