package com.smartRestaurant.inventory.dto.Product;

import com.smartRestaurant.inventory.model.State;

import java.util.List;

public record CreateProductDTO( String name,
         String description,
         double price_unit,
         double weight,
        List<String>photos,
                                double minimumStock) {
}
