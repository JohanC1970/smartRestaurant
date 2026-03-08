package com.smartRestaurant.inventory.dto.Dish;

import com.smartRestaurant.inventory.dto.recipe.GetRecipeDTO;

import java.util.List;

public record GetDishDetailDTO(String id,
                               String name,
                               String description,
                               String price,
                               List<String> photos,
                               List<GetRecipeDTO> ingredients,
                               String categoryName) {
}
