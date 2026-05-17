package com.smartRestaurant.inventory.dto.Addition;

import com.smartRestaurant.inventory.model.AdditionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record CreateAdditionDTO(
        @Length(min = 1, max = 100) @NotBlank
        String name,

        @Length(max = 500) @NotBlank
        String description,

        @NotNull
        AdditionType additionType,

        /**
         * Precio de compra por unidad. Requerido solo para adiciones SIMPLE.
         */
        Double purchasePrice,

        @Positive @NotNull
        double salePrice,

        /**
         * Unidades iniciales. Requerido para adiciones SIMPLE.
         */
        @Min(0)
        Integer units,

        /**
         * Stock mínimo para alertas. Requerido para adiciones SIMPLE.
         */
        @Min(0)
        Integer minimumStock,

        /**
         * Ingredientes de la receta. Requerido para adiciones PREPARED.
         */
        @Valid
        List<CreateAdditionRecipeDTO> recipes,

        List<String> photos
) {
}
