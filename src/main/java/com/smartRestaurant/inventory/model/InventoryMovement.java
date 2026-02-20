package com.smartRestaurant.inventory.model;


import java.time.LocalDateTime;

public class InventoryMovement {

    private String id;

    private String productId;
    private Type type; // "ENTRY" o "EXIT"
    private int quantity;
    private LocalDateTime time;
    private String user;
    private String reason;
}
