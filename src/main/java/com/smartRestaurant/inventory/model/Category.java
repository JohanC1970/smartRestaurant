package com.smartRestaurant.inventory.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Category extends BaseEntity {

    @Id
    private String id;
    @Column(length = 100)
    private String name;
    @Column(length = 300)
    private String description;
    @Enumerated(EnumType.STRING)
    private State state;
}
