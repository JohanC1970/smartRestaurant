package com.smartRestaurant.inventory.dto.drink;

import jakarta.validation.constraints.Positive;

public record DrinkMovement(
        @Positive int unit) {
}
