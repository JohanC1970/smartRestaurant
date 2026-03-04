package com.smartRestaurant.inventory.dto.Dish;

import com.smartRestaurant.inventory.dto.recipe.CreateRecipeDTO;
import com.smartRestaurant.inventory.dto.recipe.GetRecipeDTO;
import com.smartRestaurant.inventory.model.Category;
import com.smartRestaurant.inventory.model.State;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record GetDishDTO( String id,
                          String name,
                          String price,
                          String photo
) {
}
