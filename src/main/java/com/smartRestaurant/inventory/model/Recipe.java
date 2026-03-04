package com.smartRestaurant.inventory.model;

import jakarta.persistence.*;
import com.smartRestaurant.inventory.model.State;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Recipe {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "dish_id")
    private Dish dish;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private double weight;

    @Column(nullable = false)
    private String unit;

    @Enumerated(EnumType.STRING)
    private State state = State.ACTIVE;


}
