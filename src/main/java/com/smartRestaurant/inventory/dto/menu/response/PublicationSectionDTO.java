package com.smartRestaurant.inventory.dto.menu.response;

import java.util.List;

public record PublicationSectionDTO(
        String id,
        String name,
        String description,
        boolean required,
        int maxSelections,
        boolean includedInBasePrice,
        int displayOrder,
        List<SectionOptionDTO> options
) {}
