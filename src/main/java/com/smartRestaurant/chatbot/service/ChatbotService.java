package com.smartRestaurant.chatbot.service;

import com.smartRestaurant.chatbot.dto.response.ChatbotDishDTO;
import com.smartRestaurant.chatbot.dto.response.ChatbotDishDetailDTO;

import java.util.List;

/**
 * Contrato del servicio de integración con el chatbot.
 *
 * <p>Define las operaciones disponibles para que el chatbot consulte
 * información del menú del restaurante, como búsqueda de platos,
 * filtrado por categoría y consulta de ingredientes.</p>
 */
public interface ChatbotService {

    /**
     * Busca platos activos de forma flexible por nombre, descripción y/o precio máximo.
     *
     * <p>Si la búsqueda exacta no retorna resultados y {@code q} contiene varias palabras,
     * se aplica un fallback usando solo la primera palabra del término.</p>
     *
     * @param q        término de búsqueda (puede ser null para ignorar el filtro de texto)
     * @param maxPrice precio máximo permitido (puede ser null para ignorar el filtro de precio)
     * @return lista de platos que coinciden con los criterios; vacía si no hay resultados
     */
    List<ChatbotDishDTO> searchDishes(String q, Double maxPrice);

    /**
     * Retorna todos los platos activos que pertenecen a una categoría específica.
     *
     * <p>La comparación del nombre de categoría es insensible a mayúsculas/minúsculas.</p>
     *
     * @param categoryName nombre de la categoría a consultar
     * @return lista de platos activos de la categoría; vacía si no existe o no tiene platos
     */
    List<ChatbotDishDTO> getDishesByCategory(String categoryName);

    /**
     * Retorna el detalle de ingredientes activos de un plato dado su identificador.
     *
     * @param dishId identificador único del plato
     * @return DTO con el nombre del plato y su lista de ingredientes activos,
     *         o {@code null} si el plato no existe
     */
    ChatbotDishDetailDTO getDishIngredients(String dishId);
}
