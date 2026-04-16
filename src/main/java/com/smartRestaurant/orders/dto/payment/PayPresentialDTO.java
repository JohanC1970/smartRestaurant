package com.smartRestaurant.orders.dto.payment;

import com.smartRestaurant.orders.model.enums.PaymentMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para pagar una factura en modo presencial
 * El mesero registra el pago cuando el cliente lo realiza
 */
public record PayPresentialDTO(
    @NotBlank(message = "El ID de la factura es obligatorio")
    String invoiceId,
    
    @NotNull(message = "El método de pago es obligatorio")
    PaymentMethodType paymentMethod,
    
    String reference,  // Número de transacción, cheque, etc. (opcional)
    
    String notes       // Notas del pago (opcional)
) {}

