package com.smartRestaurant.inventory.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Addition extends BaseEntity {

    @Id
    private String id;

    @Column(length = 100)
    private String name;

    @Column(length = 100)
    private String description;

    @Column(length = 100)
    @Enumerated(EnumType.STRING)
    private State state;

}
