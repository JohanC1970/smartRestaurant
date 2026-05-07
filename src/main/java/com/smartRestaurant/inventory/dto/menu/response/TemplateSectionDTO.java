package com.smartRestaurant.inventory.dto.menu.response;

public record TemplateSectionDTO(
        String id,
        String name,
        String description,
        boolean required,
        int maxSelections,
        int displayOrder
) {}
