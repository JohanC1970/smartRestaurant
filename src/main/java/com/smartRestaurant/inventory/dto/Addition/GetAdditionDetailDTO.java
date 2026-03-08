package com.smartRestaurant.inventory.dto.Addition;

import java.util.List;

public record GetAdditionDetailDTO(String id,
                                   String name,
                                   String description,
                                   List<String> photos,
                                   double price) {
}
