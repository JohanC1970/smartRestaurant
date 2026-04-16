package com.smartRestaurant.orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentDTO(
        @NotBlank(message = "El ID de la orden es obligatorio")
        String orderId,

        @NotNull(message = "El ID del cliente es obligatorio")
        Long customerId,

        @Min(value = 1, message = "El monto de pago debe ser mayor a 0")
        double amount,

        @NotBlank(message = "El método de pago es obligatorio")
        String paymentMethod,

        String transactionId,
        String notes
) {}
