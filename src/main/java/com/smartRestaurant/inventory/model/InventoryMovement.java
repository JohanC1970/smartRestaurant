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
    @JoinColumn(name = "product_id")
    private Product product;

    /**
     * Categoría del ítem al que pertenece este movimiento.
     * PRODUCT → usa el FK product; DRINK / ADDITION → usa itemId e itemName.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemCategory itemCategory = ItemCategory.PRODUCT;

    /** ID de la bebida o adición (nulo para movimientos de producto). */
    @Column(length = 255)
    private String itemId;

    /** Nombre del ítem para mostrar en listados sin JOIN adicional. */
    @Column(length = 255)
    private String itemName;

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

    @Column(nullable = false)
    private double unitPrice;

    @Column(nullable = false)
    private double totalCost;
}
