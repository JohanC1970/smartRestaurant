package com.smartRestaurant.chatbot.dto.response;

import java.util.List;

/**
 * DTO con información general del restaurante para el chatbot.
 *
 * @param hours          horario de atención del restaurante
 * @param paymentMethods lista de métodos de pago aceptados
 */
public record RestaurantInfoDTO(
        String hours,
        List<String> paymentMethods
) {
}
