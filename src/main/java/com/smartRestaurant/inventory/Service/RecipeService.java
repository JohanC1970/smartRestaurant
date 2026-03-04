package com.smartRestaurant.inventory.Service;

import com.smartRestaurant.inventory.dto.recipe.CreateRecipeDTO;
import com.smartRestaurant.inventory.dto.recipe.GetRecipeDTO;
import com.smartRestaurant.inventory.model.Dish;

import java.util.List;

public interface RecipeService {

    void registerRecipe( List<CreateRecipeDTO> recipe, Dish dish);
    List<GetRecipeDTO> getRecipesByDishID(String dishID);
}
