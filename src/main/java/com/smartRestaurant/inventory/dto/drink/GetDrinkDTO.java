package com.smartRestaurant.inventory.dto.drink;

public record GetDrinkDTO(
        String id,
        String name,
        double mililiters,
        String drinkType,
        double salePrice,
        boolean alcohol,
        String photo,
        int units,
        String state
) {
}
