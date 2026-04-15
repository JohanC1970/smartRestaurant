package com.smartRestaurant.orders.dto.invoice;

import com.smartRestaurant.orders.model.enums.InvoiceStatus;

import java.time.LocalDateTime;

/**
 * DTO para obtener detalles de una factura
 */
public record GetInvoiceDTO(
    String id,
    String orderId,
    InvoiceStatus status,
    double subtotal,
    double tax,
    double total,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime paidAt
) {}

