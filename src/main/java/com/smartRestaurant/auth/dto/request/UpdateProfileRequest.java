package com.smartRestaurant.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 1, max = 100)
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 1, max = 100)
    private String lastName;
}
