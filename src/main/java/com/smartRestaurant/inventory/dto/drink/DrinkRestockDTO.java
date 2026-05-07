package com.smartRestaurant.inventory.dto.drink;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para reabastecimiento de bebidas simples.
 * Actualiza unidades Y precio de compra simultáneamente,
 * manteniendo el precio de compra alineado con el mercado actual.
 */
public record DrinkRestockDTO(
        @Positive @NotNull
        int unit,

        @Positive @NotNull
        double purchasePrice
) {
}
