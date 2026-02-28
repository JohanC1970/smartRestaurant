package com.smartRestaurant.inventory.dto.Addition;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

public record CreateAdditionDTO(@Length(min = 1, max = 100) @NotBlank(message = "El nombre no puede estar vacío")
                                String name,
                                @Length(max = 500) @NotBlank(message = "La descripción no puede estar vacía")
                                String description,
                                @NotNull @Positive
                                double price) {
}
