package com.smartRestaurant.inventory.dto.Addition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record UpdateAdditionDTO(
        @Length(min = 1, max = 100) @NotBlank
        String name,

        @Length(max = 500) @NotBlank
        String description,

        @Positive
        double salePrice,

        /**
         * Unidades actuales. Opcional, solo para SIMPLE.
         */
        @Min(0)
        Integer units,

        /**
         * Stock mínimo. Opcional, solo para SIMPLE.
         */
        @Min(0)
        Integer minimumStock,

        /**
         * Ingredientes de la receta. Opcional, solo para PREPARED.
         */
        @Valid
        List<CreateAdditionRecipeDTO> recipes,

        List<String> photos
) {
}
