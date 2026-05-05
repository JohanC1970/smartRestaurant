package com.smartRestaurant.inventory.dto.menu.request;

import com.smartRestaurant.inventory.model.MenuItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AddSectionOptionRequest(
        @NotNull MenuItemType itemType,
        @NotBlank String itemId,
        Integer maxPortions,
        @PositiveOrZero double additionalCost
) {}
