package com.smartRestaurant.orders.dto;

import java.time.LocalDateTime;

/**
 * DTO con la respuesta de Wompi después de procesar un pago
 * Contiene la información del pago confirmado
 */
public record WompiPaymentResponseDTO(
        String paymentId,
        String orderId,
        String wompiTransactionId,
        String status,
        long amount,
        String currency,
        LocalDateTime processedAt,
        String message,
        String paymentMethodType,
        String customerEmail
) {}

