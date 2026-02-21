package com.smartRestaurant.inventory.dto.Dish;

import com.smartRestaurant.inventory.model.Product;

import java.util.List;

public record UpdateDishDTO(String id,
                            String name,
                            String description,
                            List<Product> products,
                            double price) {
}
