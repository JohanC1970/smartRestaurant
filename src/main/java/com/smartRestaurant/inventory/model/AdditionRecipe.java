package com.smartRestaurant.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class AdditionRecipe {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "addition_id", nullable = false)
    private Addition addition;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private double weight;

    @Column(nullable = false)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private State state = State.ACTIVE;

}
