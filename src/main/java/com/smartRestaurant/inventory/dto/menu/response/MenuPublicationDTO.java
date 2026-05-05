package com.smartRestaurant.inventory.dto.menu.response;

import com.smartRestaurant.inventory.model.MenuStatus;
import com.smartRestaurant.inventory.model.MenuTimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MenuPublicationDTO(
        String id,
        LocalDate date,
        MenuTimeSlot timeSlot,
        double basePrice,
        int totalPortions,
        int availablePortions,
        MenuStatus status,
        String templateId,
        String templateName,
        LocalDateTime createdAt,
        List<PublicationSectionDTO> sections
) {}
