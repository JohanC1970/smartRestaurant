package com.smartRestaurant.restaurant.model;

import com.smartRestaurant.restaurant.model.enums.TableStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(name = "restaurant_tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTable {

    @Id
    private String id;

    /** Número visible de la mesa (ej: 1, 2, 3). Único por restaurante. */
    @Column(nullable = false, unique = true)
    @Positive
    private int number;

    /** Capacidad máxima de comensales. */
    @Column(nullable = false)
    @Positive
    private int capacity;

    /** Estado actual de la mesa. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TableStatus status = TableStatus.FREE;

    /** Zona o sección del restaurante (ej: "Terraza", "Interior", "Bar"). */
    @Column(length = 100)
    private String location;

    /** Permite desactivar mesas sin eliminarlas (ej: por mantenimiento). */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
