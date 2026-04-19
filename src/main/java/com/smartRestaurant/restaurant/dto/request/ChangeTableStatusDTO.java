package com.smartRestaurant.restaurant.dto.request;

import com.smartRestaurant.restaurant.model.enums.TableStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeTableStatusDTO(
        @NotNull(message = "El estado es obligatorio")
        TableStatus status
) {}
