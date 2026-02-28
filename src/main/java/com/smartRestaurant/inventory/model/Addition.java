package com.smartRestaurant.inventory.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Addition extends BaseEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500, nullable = false)
    private String description;

    @Column(length = 100,  nullable = false)
    private double price;

    @Column( nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 10,   nullable = false)
    @Enumerated(EnumType.STRING)
    private State state;

}
