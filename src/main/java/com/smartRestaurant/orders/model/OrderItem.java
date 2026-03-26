package com.smartRestaurant.orders.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class OrderItem <T> {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private T producto; // drink, dish, addition

    @ManyToOne
    @JoinColumn(nullable = false)
    private Order order;

    @Column(length = 300)
    private String notes;


}
