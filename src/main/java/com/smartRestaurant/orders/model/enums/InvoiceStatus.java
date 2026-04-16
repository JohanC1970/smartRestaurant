package com.smartRestaurant.orders.model.enums;

/**
 * Estados posibles de una factura
 */
public enum InvoiceStatus {
    PENDING,      // Factura creada, esperando pago
    PAID,         // Pagada completamente
    CANCELLED     // Cancelada/Anulada
}

