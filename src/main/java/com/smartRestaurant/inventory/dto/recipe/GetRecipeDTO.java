package com.smartRestaurant.inventory.dto.recipe;

public record GetRecipeDTO(String product_id,
                           String product_name,
                           double weight,
                           String unit) {
}
