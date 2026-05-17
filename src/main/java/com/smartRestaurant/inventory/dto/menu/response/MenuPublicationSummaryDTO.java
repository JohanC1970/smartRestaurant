package com.smartRestaurant.inventory.dto.menu.response;

import com.smartRestaurant.inventory.model.MenuStatus;
import com.smartRestaurant.inventory.model.MenuTimeSlot;

import java.time.LocalDate;

public record MenuPublicationSummaryDTO(
        String id,
        LocalDate date,
        MenuTimeSlot timeSlot,
        double basePrice,
        int totalPortions,
        int availablePortions,
        MenuStatus status,
        String templateName
) {}
