package com.smartRestaurant.inventory.dto.recipe;

public record CreateRecipeDTO(
            String product_id,
            double quantity,
            String unit
) {
}
