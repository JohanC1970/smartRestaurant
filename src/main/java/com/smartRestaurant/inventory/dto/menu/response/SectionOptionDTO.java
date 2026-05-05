package com.smartRestaurant.inventory.dto.menu.response;

import com.smartRestaurant.inventory.model.MenuItemType;

public record SectionOptionDTO(
        String id,
        MenuItemType itemType,
        String itemId,
        String itemName,
        String itemPhoto,
        double additionalCost,
        Integer maxPortions,
        Integer availablePortions,
        boolean active
) {}
