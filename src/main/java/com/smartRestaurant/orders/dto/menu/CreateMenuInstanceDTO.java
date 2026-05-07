package com.smartRestaurant.orders.dto.menu;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateMenuInstanceDTO(
        @NotBlank String publicationId,
        String seatIdentifier,
        String observation,
        @Valid List<CreateMenuSectionSelectionDTO> selections,
        List<String> excludedSectionIds
) {}
