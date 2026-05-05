package com.smartRestaurant.inventory.dto.menu.request;

import jakarta.validation.constraints.Positive;

public record AdjustPortionsRequest(
        @Positive int totalPortions
) {}
