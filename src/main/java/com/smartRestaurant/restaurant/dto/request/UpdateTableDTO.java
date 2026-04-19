package com.smartRestaurant.restaurant.dto.request;

import jakarta.validation.constraints.Positive;

public record UpdateTableDTO(
        @Positive(message = "La capacidad debe ser positiva")
        Integer capacity,

        String location
) {}
