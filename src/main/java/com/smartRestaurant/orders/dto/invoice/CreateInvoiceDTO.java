package com.smartRestaurant.orders.dto.invoice;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para crear una factura
 */
public record CreateInvoiceDTO(
    @NotBlank(message = "El ID de la orden es obligatorio")
    String orderId,
    
    @Min(value = 0, message = "El subtotal no puede ser negativo")
    double subtotal,
    
    @Min(value = 0, message = "El tax no puede ser negativo")
    double tax
) {}

