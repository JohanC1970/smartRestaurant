package com.smartRestaurant.chatbot.dto.response;

import java.util.List;

/**
 * DTO con el detalle de ingredientes de un plato para las respuestas del chatbot.
 *
 * <p>Usado principalmente para que el chatbot pueda informar al cliente
 * sobre los ingredientes de un plato y validar posibles alergias.</p>
 *
 * @param dishName    nombre del plato
 * @param ingredients lista de nombres de ingredientes activos del plato
 */
public record ChatbotDishDetailDTO(
        String dishName,
        List<String> ingredients
) {
}
