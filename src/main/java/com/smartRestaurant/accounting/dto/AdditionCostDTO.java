package com.smartRestaurant.accounting.dto;

import lombok.Builder;

@Builder
public record AdditionCostDTO(
        String additionId,
        String additionName,
        String additionType,
        double salePrice,
        Double purchasePrice,
        Double estimatedCost,
        double margin,
        int units) {
}
