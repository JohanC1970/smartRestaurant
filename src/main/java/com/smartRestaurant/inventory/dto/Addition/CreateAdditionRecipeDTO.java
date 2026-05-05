package com.smartRestaurant.inventory.dto.Addition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateAdditionRecipeDTO(
        @NotBlank
        String productId,

        @Positive @NotNull
        double weight,

        @NotBlank
        String unit
) {
}
