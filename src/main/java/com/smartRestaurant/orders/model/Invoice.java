package com.smartRestaurant.orders.model;

import com.smartRestaurant.orders.model.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity que representa una factura de una orden
 * Se genera automáticamente cuando una orden está completada
 */
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    
    @Id
    private String id;  // INV-YYYYMMDD-XXXXX
    
    @OneToOne
    @JoinColumn(nullable = false, unique = true)
    private Order order;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;  // PENDING, PAID, CANCELLED
    
    @Column(nullable = false)
    private double subtotal;  // Suma de items (sin IVA)
    
    @Column(nullable = false)
    private double tax;  // IVA (8%)
    
    @Column(nullable = false)
    private double total;  // subtotal + tax
    
    @OneToOne(mappedBy = "invoice", cascade = CascadeType.ALL)
    private Payment payment;  // Relación con Payment
    
    @Column(length = 500)
    private String notes;  // Notas adicionales
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime paidAt;  // Cuándo se pagó
    
    @Version
    private Long version;  // Para optimistic locking
}

