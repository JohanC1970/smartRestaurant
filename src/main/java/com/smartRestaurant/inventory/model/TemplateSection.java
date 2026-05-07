package com.smartRestaurant.inventory.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TemplateSection {

    @Id
    private String id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private boolean required;

    @Column(nullable = false)
    private int maxSelections = 1;

    @Column(nullable = false)
    private int displayOrder;

    @ManyToOne
    @JoinColumn(nullable = false)
    private MenuTemplate template;
}
