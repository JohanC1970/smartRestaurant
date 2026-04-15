package com.smartRestaurant.orders.model.enums;

/**
 * Estado del pago de una orden
 */
public enum OrderPaymentStatus {
    NOT_REQUIRED,    // Orden presencial (se paga después)
    PENDING,         // Orden online (esperando pago)
    CONFIRMED,       // Pago confirmado
    REFUNDED         // Reembolsado
}

