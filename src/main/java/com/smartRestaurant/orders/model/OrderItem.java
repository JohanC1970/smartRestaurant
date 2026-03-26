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

    @ManyToOne
    @JoinColumn(nullable = false)
    private Order order;

    @Column(length = 300)
    private String notes;
}
