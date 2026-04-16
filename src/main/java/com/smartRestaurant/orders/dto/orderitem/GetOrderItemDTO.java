package com.smartRestaurant.orders.dto.orderitem;

/**
 * DTO para obtener los detalles de un item de orden
 */
public record GetOrderItemDTO(
        String id,
        String productId,
        String productName,
        String productType,  // DISH, DRINK, ADDITION
        int quantity,
        double unitPrice,
        double totalPrice,
        String notes
) {}

