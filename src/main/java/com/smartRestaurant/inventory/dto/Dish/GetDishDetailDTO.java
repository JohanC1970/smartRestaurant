package com.smartRestaurant.inventory.dto.Dish;

import com.smartRestaurant.inventory.dto.recipe.GetRecipeDTO;
import com.smartRestaurant.inventory.model.DishAvailability;

import java.util.List;

public record GetDishDetailDTO(String id,
                               String name,
                               String description,
                               double price,
                               List<String> photos,
                               List<GetRecipeDTO> ingredients,
                               String categoryId,
                               String categoryName,
                               double estimatedCost,
                               double margin,
                               DishAvailability availability) {
}
