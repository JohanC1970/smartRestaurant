package com.smartRestaurant.inventory.dto.InventoryMovement;

import com.smartRestaurant.inventory.model.ItemCategory;
import com.smartRestaurant.inventory.model.Type;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetInventoryMovementDTO(
        String productId,
        String productName,
        ItemCategory itemCategory,
        Type type,
        double weight,
        double unitPrice,
        double totalCost,
        LocalDateTime timeAt,
        String userName,
        String reason) {
}
