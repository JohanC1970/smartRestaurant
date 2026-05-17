package com.smartRestaurant.inventory.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SectionOption {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuItemType itemType;

    // Solo uno de los tres estará poblado, según itemType
    @ManyToOne
    private Dish dish;

    @ManyToOne
    private Drink drink;

    @ManyToOne
    private Addition addition;

    /**
     * Límite de porciones para esta opción específica.
     * null = sin límite propio (solo aplica el total de la publicación).
     */
    @Column
    private Integer maxPortions;

    @Column
    private Integer availablePortions;

    /**
     * Costo adicional sobre el basePrice del menú.
     * 0 = incluida en el precio base.
     */
    @Column(nullable = false)
    private double additionalCost = 0.0;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne
    @JoinColumn(nullable = false)
    private PublicationSection section;
}
