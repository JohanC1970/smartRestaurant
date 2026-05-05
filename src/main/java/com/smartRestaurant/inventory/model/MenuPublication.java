package com.smartRestaurant.inventory.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class MenuPublication {

    @Id
    private String id;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuTimeSlot timeSlot;

    @Column(nullable = false)
    private double basePrice;

    @Column(nullable = false)
    private int totalPortions;

    @Column(nullable = false)
    private int availablePortions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuStatus status = MenuStatus.DRAFT;

    @ManyToOne
    private MenuTemplate template;

    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("displayOrder ASC")
    private List<PublicationSection> sections = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
