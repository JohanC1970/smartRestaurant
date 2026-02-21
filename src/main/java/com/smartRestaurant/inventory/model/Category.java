package com.smartRestaurant.inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Category extends BaseEntity {

    @Id
    private String id;
    private String name;
    private String description;
    private State state;
}
