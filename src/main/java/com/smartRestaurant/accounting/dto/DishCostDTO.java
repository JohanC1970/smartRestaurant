package com.smartRestaurant.accounting.dto;

import lombok.Builder;

@Builder
public record DishCostDTO(
        String dishId,
        String dishName,
        double dishPrice,
        double estimatedCost,
        double margin) {
}
