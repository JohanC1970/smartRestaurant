package com.smartRestaurant.orders.model;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.orders.model.enums.OrderChannel;
import com.smartRestaurant.orders.model.enums.OrderPaymentStatus;
import com.smartRestaurant.orders.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // pending, in_progress, completed, delivered, cancelled

    @Enumerated(EnumType.STRING)
    private OrderChannel channel; // online, presencial

    @ManyToOne
    private User customer; // null if was presential

    @ManyToOne
    private User waiter; //  null if was online

    private String tableNumber; // null if was online

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt; // null if don´t

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items;

    @OneToOne(mappedBy = "order")
    private Payment payment; // null if was presential

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Invoice invoice;  // La factura de esta orden

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderPaymentStatus paymentStatus = OrderPaymentStatus.NOT_REQUIRED;  // Estado de pago
}
