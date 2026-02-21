package com.smartRestaurant.inventory.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Drink extends BaseEntity{

    private String id;
    private String name;
    private String description;
    private String mililiters;
    private State state;
    private boolean alcohol;
    private Category category;
    private List<String> photos;



}
