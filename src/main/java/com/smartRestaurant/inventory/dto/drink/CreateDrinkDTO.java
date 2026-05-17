package com.smartRestaurant.inventory.dto.drink;

import com.smartRestaurant.inventory.model.DrinkType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record CreateDrinkDTO(
        @NotBlank @Length(min = 1, max = 50)
        String name,

        @NotBlank @Length(min = 10, max = 500)
        String description,

        @Positive @NotNull
        double mililiters,

        @NotNull
        DrinkType drinkType,

        /**
         * Precio de compra por unidad. Requerido solo para bebidas SIMPLE.
         * La validación condicional se realiza en el servicio.
         */
        Double purchasePrice,

        @Positive @NotNull
        double salePrice,

        @NotNull
        boolean alcohol,

        @NotNull @Size(min = 1, max = 3)
        List<String> photos,

        /**
         * Unidades iniciales. Requerido para bebidas SIMPLE.
         */
        Integer units,

        /**
         * Stock mínimo para alertas. Requerido para bebidas SIMPLE.
         */
        @Min(0)
        Integer minimumStock,

        /**
         * Ingredientes de la receta. Requerido para bebidas PREPARED.
         */
        @Valid
        List<CreateDrinkRecipeDTO> recipes
) {
}
