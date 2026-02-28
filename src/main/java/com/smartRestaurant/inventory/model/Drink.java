package com.smartRestaurant.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Drink extends BaseEntity{

    @Id
    private String id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 500, nullable = false)
    private String description;

    @Column(nullable = false)
    @Positive
    private double mililiters;

    @Enumerated(EnumType.STRING)
    private State state;

    @Column(nullable = false)
    private boolean alcohol;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private Category category;

    @ElementCollection
    @CollectionTable(name = "Drink_photos", joinColumns = @JoinColumn(name = "drink_id"))
    @Column(name = "photo")
    private List<String> photos;

    @Column(nullable = false) @Positive
    private int units;


}
