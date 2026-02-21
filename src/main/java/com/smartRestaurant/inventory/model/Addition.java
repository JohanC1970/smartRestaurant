package com.smartRestaurant.inventory.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Addition extends BaseEntity {

    private String id;
    private String name;
    private String description;
    private State state;

}
