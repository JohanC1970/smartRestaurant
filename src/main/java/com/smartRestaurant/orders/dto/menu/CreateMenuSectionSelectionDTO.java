package com.smartRestaurant.orders.dto.menu;

import jakarta.validation.constraints.NotBlank;

public record CreateMenuSectionSelectionDTO(
        @NotBlank String sectionId,
        @NotBlank String optionId,
        String observation
) {}
