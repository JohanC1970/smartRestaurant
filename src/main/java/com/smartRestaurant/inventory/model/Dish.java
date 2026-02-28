package com.smartRestaurant.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Dish extends BaseEntity {

    @Id
    private String id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    private String description;

    @Column( nullable = false)
    @Positive
    private String price;

    @ElementCollection
    @CollectionTable(name = "Dish_photos", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "photos")
    private List<String> photos;

    @Enumerated(EnumType.STRING)
    private State state;

    @ManyToOne
    private Category category;

    @OneToMany(cascade = CascadeType.ALL)
    @CollectionTable(name = "Additions", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "addition")
    private List<Addition> additions;

}
