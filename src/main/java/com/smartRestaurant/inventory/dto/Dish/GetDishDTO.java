package com.smartRestaurant.inventory.dto.Dish;

import com.smartRestaurant.inventory.model.DishAvailability;

public record GetDishDTO(String id,
                         String name,
                         double price,
                         String photo,
                         DishAvailability availability) {
}
