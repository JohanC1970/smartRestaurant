package com.smartRestaurant.inventory.dto.menu.request;

import com.smartRestaurant.inventory.model.MenuTimeSlot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public record CreateMenuPublicationRequest(
        @NotNull LocalDate date,
        @NotNull MenuTimeSlot timeSlot,
        @Positive double basePrice,
        @Positive int totalPortions,
        String templateId,
        @Valid List<AddPublicationSectionRequest> sections
) {}
