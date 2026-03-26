package com.smartRestaurant.orders.dto.Order;

import com.smartRestaurant.orders.model.enums.OrderChannel;
import com.smartRestaurant.orders.model.enums.OrderStatus;

import java.time.LocalDateTime;

/**
 * DTO para listar órdenes (resumen sin detalle completo)
 */
public record GetOrdersDTO(
        String id,
        OrderStatus status,
        OrderChannel channel,
        String customerName,
        LocalDateTime createdAt,
        int itemCount,
        double totalAmount
) {}
