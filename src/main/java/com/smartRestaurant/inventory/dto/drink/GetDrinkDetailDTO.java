package com.smartRestaurant.inventory.dto.drink;

import java.util.List;

public record GetDrinkDetailDTO(
        String id,
        String name,
        String description,
        double mililiters,
        String drinkType,
        Double purchasePrice,
        double salePrice,
        Double estimatedCost,
        Double margin,
        boolean alcohol,
        String photo,
        int units,
        int minimumStock,
        String state,
        String categoryId,
        String categoryName,
        List<GetDrinkRecipeDTO> recipes
) {
}
