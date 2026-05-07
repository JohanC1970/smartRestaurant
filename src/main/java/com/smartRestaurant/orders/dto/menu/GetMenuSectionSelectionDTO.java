package com.smartRestaurant.orders.dto.menu;

public record GetMenuSectionSelectionDTO(
        String sectionId,
        String sectionName,
        String optionId,
        String optionName,
        double additionalCost,
        String observation
) {}
