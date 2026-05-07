package com.smartRestaurant.orders.dto.menu;

import java.util.List;

public record GetMenuInstanceDTO(
        String id,
        String publicationId,
        String publicationDate,
        String timeSlot,
        double basePrice,
        String seatIdentifier,
        String observation,
        List<GetMenuSectionSelectionDTO> selections,
        List<GetMenuSectionExclusionDTO> exclusions
) {}
