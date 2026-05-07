package com.smartRestaurant.inventory.dto.drink;

public record GetDrinkRecipeDTO(
        String productId,
        String productName,
        double weight,
        String unit
) {
}
