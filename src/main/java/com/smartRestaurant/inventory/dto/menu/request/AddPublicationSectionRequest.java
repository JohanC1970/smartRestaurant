package com.smartRestaurant.inventory.dto.menu.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record AddPublicationSectionRequest(
        @NotBlank @Length(max = 100) String name,
        @Length(max = 300) String description,
        boolean required,
        @Min(1) int maxSelections,
        boolean includedInBasePrice,
        int displayOrder
) {}
