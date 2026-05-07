package com.smartRestaurant.inventory.dto.Addition;

public record GetAdditionRecipeDTO(
        String productId,
        String productName,
        double weight,
        String unit
) {
}
