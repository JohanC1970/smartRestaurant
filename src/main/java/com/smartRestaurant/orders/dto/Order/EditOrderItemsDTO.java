package com.smartRestaurant.orders.dto.Order;

import com.smartRestaurant.orders.dto.orderitem.CreateOrderItemDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record EditOrderItemsDTO(
        @NotEmpty(message = "La lista de items no puede estar vacía")
        @Valid
        List<CreateOrderItemDTO> items
) {
}
