package com.smartRestaurant.inventory.model;

import com.smartRestaurant.auth.model.entity.User;

import java.time.LocalDateTime;

public abstract class BaseEntity {

    private String id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private User createdBy;

    private User updatedBy;
}
