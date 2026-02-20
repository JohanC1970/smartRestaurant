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
    private String price;
    private String weight;
    List<String> photos;
    private State state;
    private int minimumStock;

    // stock en revisión
    private int stock;
}
