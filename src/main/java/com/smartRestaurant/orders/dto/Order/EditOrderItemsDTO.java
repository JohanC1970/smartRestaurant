package com.smartRestaurant.orders.dto.Order;

import com.smartRestaurant.orders.dto.orderitem.CreateOrderItemDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record EditOrderItemsDTO(
    @NotEmpty(message = "Debe incluir al menos un item")
    @Valid
    List<CreateOrderItemDTO> items
) {}
