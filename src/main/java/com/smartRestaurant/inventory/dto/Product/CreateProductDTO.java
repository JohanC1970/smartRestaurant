package com.smartRestaurant.inventory.dto.Product;

public record CreateProductDTO(String name,
                               String description,
                               String quantity,
                               double weight) {
}
