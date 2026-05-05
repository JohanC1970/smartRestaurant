package com.smartRestaurant.accounting.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AccountingSummaryDTO(
        double totalExpenses,
        double totalRevenue,
        double inventoryCapital,
        double drinkCapital,
        double additionCapital,
        double estimatedProfit,
        List<DishCostDTO> dishCosts,
        List<DrinkCostDTO> drinkCosts,
        List<AdditionCostDTO> additionCosts) {
}
