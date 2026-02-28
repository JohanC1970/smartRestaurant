package com.smartRestaurant.inventory.dto.Product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record StockMovementDTO(@Positive @NotBlank double weight) {
}
