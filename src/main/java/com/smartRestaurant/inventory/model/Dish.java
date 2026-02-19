package com.smartRestaurant.inventory.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

public class Dish extends BaseEntity {

    private String id;
    private String name;
    private String description;
    private String price;
    private List<String> photos;
    private State state;

}
