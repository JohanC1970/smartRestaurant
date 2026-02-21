package com.smartRestaurant.inventory.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Product {

    private String id;
    private String name;
    private String description;
    private double price_unit;
    private double weight;
    List<String> photos;
    private State state;
    private int minimumStock;

    // stock en revisión
    private double stock;
}
