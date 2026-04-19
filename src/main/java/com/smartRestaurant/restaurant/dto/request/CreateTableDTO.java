package com.smartRestaurant.restaurant.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;

public record CreateTableDTO(
        @NotNull(message = "El número de mesa es obligatorio")
        @Positive(message = "El número de mesa debe ser positivo")
        Integer number,

        @NotNull(message = "La capacidad es obligatoria")
        @Positive(message = "La capacidad debe ser positiva")
        Integer capacity,

        String location
) {}
