package com.smartRestaurant.inventory.dto.drink;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record UpdateDrinkDTO(
        @NotBlank @Length(min = 1, max = 50)
        String name,

        @NotBlank @Length(min = 10, max = 500)
        String description,

        @Positive @NotNull
        double mililiters,

        @Positive @NotNull
        double salePrice,

        @NotNull
        boolean alcohol,

        @NotNull @Size(min = 1, max = 3)
        List<String> photos,

        /**
         * Solo para bebidas SIMPLE. Ignorado en bebidas PREPARED.
         */
        @Min(0)
        Integer units,

        /**
         * Solo para bebidas SIMPLE. Ignorado en bebidas PREPARED.
         */
        @Min(0)
        Integer minimumStock,

        /**
         * Solo para bebidas PREPARED. Si se envía, reemplaza todas las recetas actuales.
         */
        @Valid
        List<CreateDrinkRecipeDTO> recipes
) {
}
