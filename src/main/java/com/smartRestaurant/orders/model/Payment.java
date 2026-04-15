package com.smartRestaurant.orders.model;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.orders.model.enums.PaymentMethodType;
import com.smartRestaurant.orders.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Payment {

    @Id
    private String id;

    @OneToOne
    @JoinColumn(nullable = false, unique = true)
    private Order order;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User customer;

    @OneToOne
    @JoinColumn(nullable = false, unique = true)
    private Invoice invoice;  // La factura que se paga

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethodType paymentMethod;  // Cambiado de String a Enum

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime paidAt;  // Fecha cuando se pagó

    @Column(length = 300)
    private String transactionId;  // ID de la transacción (si es con tarjeta, etc.)

    @Column(length = 300)
    private String notes; // comentarios adicionales

}
