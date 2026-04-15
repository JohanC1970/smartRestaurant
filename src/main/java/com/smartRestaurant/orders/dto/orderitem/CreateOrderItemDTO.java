package com.smartRestaurant.orders.dto.orderitem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * DTO para crear items de una orden
 * Se usa para Dish, Drink o Addition
 */
public record CreateOrderItemDTO(
        @NotBlank(message = "El ID del producto es obligatorio")
        String productId,
        
        @NotBlank(message = "El tipo de producto es obligatorio")
        String productType,  // DISH, DRINK, ADDITION
        
        @Positive(message = "La cantidad debe ser mayor a 0")
        int quantity,
        
        String notes
) {}

