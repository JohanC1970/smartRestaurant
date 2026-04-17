package com.smartRestaurant.orders.dto.Order;

import com.smartRestaurant.orders.dto.orderitem.GetOrderItemDTO;
import com.smartRestaurant.orders.model.enums.OrderChannel;
import com.smartRestaurant.orders.model.enums.OrderStatus;
import com.smartRestaurant.restaurant.model.enums.TableStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para obtener los detalles completos de una orden
 */
public record GetOrderDetailDTO(
        String id,
        OrderStatus status,
        OrderChannel channel,
        String customerName,
        String waiterId,
        TableInfo table,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<GetOrderItemDTO> items,
        double totalAmount,
        String paymentStatus
) {
    /** Información resumida de la mesa asignada a la orden. Null si la orden es online. */
    public record TableInfo(String id, int number, int capacity, String location, TableStatus status) {}
}
