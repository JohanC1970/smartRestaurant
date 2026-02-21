package com.smartRestaurant.inventory.model;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class InventoryMovement {

    private String id;

    private String productId;
    private Type type; // "ENTRY" o "EXIT"
    private double weight;
    private LocalDateTime time;
    private String user;
    private String reason;
}
