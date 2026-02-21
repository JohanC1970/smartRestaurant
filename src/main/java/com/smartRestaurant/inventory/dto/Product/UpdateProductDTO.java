package com.smartRestaurant.inventory.dto.Product;

import java.util.List;

public record UpdateProductDTO(String name,
                               String description,
                               double price_unit,
                               double weight,
                               List<String> photos,
                               int minimumStock,
                               // stock en revisión
                               double stock) {
}
