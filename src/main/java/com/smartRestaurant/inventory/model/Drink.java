package com.smartRestaurant.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Drink extends BaseEntity {

    @Id
    private String id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 500, nullable = false)
    private String description;

    @Column(nullable = false)
    private double mililiters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private State state;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DrinkType drinkType;

    /**
     * Precio de venta al cliente. Siempre requerido.
     */
    @Column(nullable = false)
    private double salePrice;

    /**
     * Precio de compra por unidad. Solo aplica para bebidas SIMPLE.
     * Se actualiza automáticamente en cada reabastecimiento.
     */
    @Column
    private Double purchasePrice;

    @Column(nullable = false)
    private boolean alcohol;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private Category category;

    @ElementCollection
    @CollectionTable(name = "Drink_photos", joinColumns = @JoinColumn(name = "drink_id"))
    @Column(name = "photo")
    private List<String> photos;

    /**
     * Unidades en stock. Solo relevante para bebidas SIMPLE.
     * Para bebidas PREPARED, este valor permanece en 0.
     */
    @Column(nullable = false)
    @PositiveOrZero
    private int units;

    @Column(nullable = false)
    @PositiveOrZero
    private int reservedUnits;

    /**
     * Stock mínimo para alertas. Solo relevante para bebidas SIMPLE.
     */
    @Column(nullable = false)
    @PositiveOrZero
    private int minimumStock;

    /**
     * Receta de ingredientes. Solo aplica para bebidas PREPARED.
     */
    @OneToMany(mappedBy = "drink", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Where(clause = "state = 'ACTIVE'")
    private List<DrinkRecipe> recipes = new ArrayList<>();

}
