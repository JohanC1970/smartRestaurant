package com.smartRestaurant.inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.util.List;
@Entity
public class DailyMenu {

    @Id
    private String id;

    @ManyToOne
    private Dish dish;
}
