package com.smartRestaurant.orders.model;

import com.smartRestaurant.inventory.model.Addition;
import com.smartRestaurant.inventory.model.Dish;
import com.smartRestaurant.inventory.model.Drink;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyDiscriminatorValue;
import org.hibernate.annotations.AnyKeyJavaClass;

@Entity
@Getter
@Setter
public class OrderItem {

    @Id
    private String id;

    @Any
    @AnyKeyJavaClass(String.class)
    @Column(name = "producto_type")
    @AnyDiscriminatorValue(discriminator = "DISH", entity = Dish.class)
    @AnyDiscriminatorValue(discriminator = "DRINK", entity = Drink.class)
    @AnyDiscriminatorValue(discriminator = "ADDITION", entity = Addition.class)
    @JoinColumn(name = "producto_id", nullable = false)
    private Object producto;

    @Column(nullable = false)
    private int quantity;

    /**
     * Precio unitario congelado al momento de crear el pedido.
     * No debe depender del precio actual del catálogo.
     */
    @Column(nullable = false)
    private double unitPrice;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Order order;

    @Column(length = 300)
    private String notes;
}
