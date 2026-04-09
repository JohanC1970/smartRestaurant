package com.smartRestaurant.orders.dto.Order;

import com.smartRestaurant.orders.dto.orderitem.CreateOrderItemDTO;
import com.smartRestaurant.orders.model.enums.OrderChannel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO para crear una nueva orden con todos sus items
 * Usa record para inmutabilidad y simplificación
 */
public record CreateOrderDto(
        @NotNull(message = "El canal de la orden es obligatorio")
        OrderChannel channel,
        
        Long customerId,           // null si es presencial
        Long waiterId,             // null si es online
        String tableNumber,        // null si es online
        
        @NotEmpty(message = "La orden debe tener al menos un item")
        @Valid
        List<CreateOrderItemDTO> items
) {}
