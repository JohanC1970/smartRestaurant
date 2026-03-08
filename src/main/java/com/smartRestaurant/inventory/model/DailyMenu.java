package com.smartRestaurant.inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Entity
@Getter
@Setter
public class DailyMenu {

    @Id
    private String id;

    @ManyToOne
    private Dish dish;
}
