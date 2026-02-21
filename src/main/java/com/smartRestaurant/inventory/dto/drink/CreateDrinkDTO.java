package com.smartRestaurant.inventory.dto.drink;

import com.smartRestaurant.inventory.model.Category;
import com.smartRestaurant.inventory.model.State;

import java.util.List;

public record CreateDrinkDTO( String name,
         String description,
         String mililiters,
         State state,
         boolean alcohol,
         Category category,
         List<String>photos) {
}
