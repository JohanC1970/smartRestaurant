package com.smartRestaurant.inventory.dto.Product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockMovementDTO(
        @Positive @NotNull Double weight,
        Double unitPrice,
        String reason) {
}
