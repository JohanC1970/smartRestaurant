package com.smartRestaurant.inventory.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Category extends BaseEntity {

    @Id
    private String id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 300, nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private State state;
}
