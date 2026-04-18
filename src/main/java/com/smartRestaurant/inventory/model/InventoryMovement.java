package com.smartRestaurant.inventory.model;


import com.smartRestaurant.auth.model.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type; // "ENTRY" o "EXIT"

    @Positive
    @Column(nullable = false)
    private double weight;

    @Column(nullable = false)
    private LocalDateTime timeAt;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String reason;
}
