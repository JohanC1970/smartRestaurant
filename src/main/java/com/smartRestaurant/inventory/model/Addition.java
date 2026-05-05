package com.smartRestaurant.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdditionType additionType;

    /**
     * Precio de venta al cliente. Siempre requerido.
     */
    @Column(nullable = false)
    private double salePrice;

    /**
     * Precio de compra por unidad. Solo aplica para adiciones SIMPLE.
     * Se actualiza automáticamente en cada reabastecimiento.
     */
    @Column
    private Double purchasePrice;

    @Column(length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private State state;

    /**
     * Unidades en stock. Solo relevante para adiciones SIMPLE.
     * Para adiciones PREPARED, este valor permanece en 0.
     */
    @Column(nullable = false)
    @PositiveOrZero
    private int units;

    @Column(nullable = false)
    @PositiveOrZero
    private int reservedUnits = 0;

    /**
     * Stock mínimo para alertas. Solo relevante para adiciones SIMPLE.
     */
    @Column(nullable = false)
    @PositiveOrZero
    private int minimumStock;

    @ElementCollection
    @CollectionTable(name = "Addition_photos", joinColumns = @JoinColumn(name = "addition_id"))
    @Column(nullable = false)
    private List<String> photos;

    /**
     * Receta de ingredientes. Solo aplica para adiciones PREPARED.
     */
    @OneToMany(mappedBy = "addition", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Where(clause = "state = 'ACTIVE'")
    private List<AdditionRecipe> recipes = new ArrayList<>();

}
