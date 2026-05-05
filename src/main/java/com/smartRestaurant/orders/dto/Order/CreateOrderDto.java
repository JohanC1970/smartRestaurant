package com.smartRestaurant.orders.dto.Order;

import com.smartRestaurant.orders.dto.menu.CreateMenuInstanceDTO;
import com.smartRestaurant.orders.dto.orderitem.CreateOrderItemDTO;
import com.smartRestaurant.orders.model.enums.OrderChannel;
import jakarta.validation.Valid;
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
        String tableId,            // null si es online — ID de RestaurantTable

        @Valid
        List<CreateOrderItemDTO> items,

        @Valid
        List<CreateMenuInstanceDTO> menuInstances  // null o vacío si no se pide menú del día
) {}
