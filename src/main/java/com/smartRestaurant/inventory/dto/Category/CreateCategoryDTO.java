package com.smartRestaurant.inventory.dto.Category;

import java.util.List;

public record CreateCategoryDTO(String name,
                                String description,
                                List<String> photos) {
    // preguntar si
}
