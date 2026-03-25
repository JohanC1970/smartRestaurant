package com.smartRestaurant.orders.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class OrderItem {

    @Id
    private String id;
}
