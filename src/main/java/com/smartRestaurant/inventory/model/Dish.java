package com.smartRestaurant.inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Dish extends BaseEntity {

    @Id
    private String id;
    private String name;
    private String description;
    private String price;
    private List<String> photos;
    private State state;
    @ManyToOne
    private Category category;
    private List<Addition> additions;

}
