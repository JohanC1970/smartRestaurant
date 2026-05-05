package com.smartRestaurant.inventory.dto.Addition;

public record GetAdditionDTO(
        String id,
        String name,
        String photo,
        String additionType,
        double salePrice,
        int units,
        int availableUnits,
        String state
) {
}
