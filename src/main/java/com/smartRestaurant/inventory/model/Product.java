package com.smartRestaurant.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Product {

    @Id
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String description;

    @Column(nullable = false)
    @Positive
    private double price;

    @Column(nullable = false)
    @Positive
    private double weight;

    @ElementCollection
    @CollectionTable(name = "Product_photos", joinColumns = @JoinColumn(name = "Product_id"))
    @Column(nullable = false)
    List<String> photos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private State state;

    @Column(nullable = false)
    @Positive
    private double minimumStock;

    @ManyToOne(cascade = CascadeType.ALL)
    private Suplier suplier;

}
