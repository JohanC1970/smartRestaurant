package com.smartRestaurant.inventory.dto.drink;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateDrinkRecipeDTO(
        @NotBlank
        String productId,

        @Positive @NotNull
        double weight,

        @NotBlank
        String unit
) {
}
