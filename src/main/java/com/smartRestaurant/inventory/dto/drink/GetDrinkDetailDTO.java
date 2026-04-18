package com.smartRestaurant.inventory.dto.drink;

import java.util.List;

public record GetDrinkDetailDTO(String id,
                                String name,
                                String description,
                                double mililiters,
                                boolean alcohol,
                                String photo,
                                int units,
                                int minimumStock,
                                String state,
                                String categoryName) {
}
