package com.smartRestaurant.accounting.dto;

import lombok.Builder;

@Builder
public record DrinkCostDTO(
        String drinkId,
        String drinkName,
        String drinkType,
        double salePrice,
        Double purchasePrice,
        Double estimatedCost,
        double margin,
        int units) {
}
