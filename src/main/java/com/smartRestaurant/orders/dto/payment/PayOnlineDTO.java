package com.smartRestaurant.orders.dto.payment;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para pagar una factura en modo online con Wompi
 * El cliente realiza el pago a través de Wompi
 */
public record PayOnlineDTO(
    @NotBlank(message = "El ID de la factura es obligatorio")
    String invoiceId,
    
    @NotBlank(message = "El token de Wompi es obligatorio")
    String wompiToken,
    
    @NotBlank(message = "El email del cliente es obligatorio")
    String customerEmail,
    
    String customerPhone,
    
    String notes
) {}

