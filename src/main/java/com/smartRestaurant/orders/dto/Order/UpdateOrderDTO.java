package com.smartRestaurant.orders.dto.Order;

import com.smartRestaurant.orders.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para actualizar una orden existente
 */
public record UpdateOrderDTO(
        @NotNull(message = "El estado de la orden es obligatorio")
        OrderStatus status,

        String notes
) {}
