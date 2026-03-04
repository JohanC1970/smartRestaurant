package com.smartRestaurant.inventory.dto.InventoryMovement;

import com.smartRestaurant.inventory.model.Type;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetInventoryMovementDTO(
        String productId,
        Type type,
        double weight,
        LocalDateTime timeAt,
       // String userName,
        String reason) {
}
