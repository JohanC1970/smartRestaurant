package com.smartRestaurant.inventory.dto.menu.request;

import com.smartRestaurant.inventory.model.MenuTimeSlot;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record UpdateMenuPublicationRequest(
        @NotNull LocalDate date,
        @NotNull MenuTimeSlot timeSlot,
        @Positive double basePrice,
        @Positive int totalPortions
) {}
