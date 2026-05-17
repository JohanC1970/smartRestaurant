package com.smartRestaurant.inventory.dto.Addition;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para reabastecimiento de adiciones simples.
 * Actualiza unidades Y precio de compra simultáneamente.
 */
public record AdditionRestockDTO(
        @Positive @NotNull
        int unit,

        @Positive @NotNull
        double purchasePrice
) {
}
