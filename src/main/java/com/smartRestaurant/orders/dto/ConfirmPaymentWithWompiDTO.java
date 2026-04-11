package com.smartRestaurant.orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para confirmar un pago usando Wompi
 * Se envía desde el cliente cuando decide pagar con Wompi (pasarela colombiana)
 * 
 * @param orderId ID de la orden
 * @param customerId ID del cliente
 * @param wompiToken Token de Wompi generado en el cliente (JavaScript SDK de Wompi)
 * @param amount Monto en centavos COP (100 = $1.00 COP)
 * @param description Descripción del pago para Wompi
 * @param customerEmail Email del cliente
 * @param customerPhone Teléfono del cliente
 * @param notes Notas adicionales opcionales
 */
public record ConfirmPaymentWithWompiDTO(
        @NotBlank(message = "El ID de la orden es obligatorio")
        String orderId,

        @NotNull(message = "El ID del cliente es obligatorio")
        Long customerId,

        @NotBlank(message = "El token de Wompi es obligatorio")
        String wompiToken,

        @Min(value = 100, message = "El monto mínimo es $100 COP")
        long amount,

        @NotBlank(message = "La descripción del pago es obligatoria")
        String description,

        @NotBlank(message = "El email del cliente es obligatorio")
        String customerEmail,

        String customerPhone,

        String notes
) {}

