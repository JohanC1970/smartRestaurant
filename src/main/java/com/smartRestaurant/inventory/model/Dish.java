package com.smartRestaurant.inventory.model;

import java.util.List;

public class Dish extends BaseEntity {

    private String id;
    private String name;
    private String description;
    private String price;
    private List<String> photos;

}
