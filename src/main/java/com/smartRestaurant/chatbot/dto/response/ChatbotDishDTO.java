package com.smartRestaurant.chatbot.dto.response;

/**
 * DTO con el resumen de un plato para las respuestas del chatbot.
 *
 * @param id           identificador único del plato
 * @param name         nombre del plato
 * @param price        precio del plato
 * @param description  descripción del plato
 * @param categoryName nombre de la categoría a la que pertenece el plato
 */
public record ChatbotDishDTO(
        String id,
        String name,
        double price,
        String description,
        String categoryName
) {
}
