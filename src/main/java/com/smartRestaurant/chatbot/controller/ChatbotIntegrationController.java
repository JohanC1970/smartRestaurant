package com.smartRestaurant.chatbot.controller;

import com.smartRestaurant.chatbot.dto.response.ChatbotDishDTO;
import com.smartRestaurant.chatbot.dto.response.ChatbotDishDetailDTO;
import com.smartRestaurant.chatbot.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la integración con el chatbot del restaurante.
 *
 * <p>Expone endpoints que permiten al chatbot consultar el menú,
 * filtrar platos por categoría y obtener ingredientes de un plato específico.</p>
 *
 * <p>Base URL: {@code /api/chatbot}</p>
 */
@RestController
@RequestMapping("/api/chatbot")
public class ChatbotIntegrationController {

    private final ChatbotService chatbotService;

    /**
     * @param chatbotService servicio con la lógica de negocio del chatbot
     */
    public ChatbotIntegrationController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    /**
     * Busca platos activos de forma flexible por nombre, descripción y/o precio máximo.
     *
     * <p>Ambos parámetros son opcionales. Si se omiten, retorna todos los platos activos.</p>
     *
     * @param q        término de búsqueda libre (nombre o descripción del plato)
     * @param maxPrice precio máximo para filtrar resultados
     * @return {@code 200 OK} con la lista de platos que coinciden con los criterios
     */
    @GetMapping("/dishes/search")
    public ResponseEntity<List<ChatbotDishDTO>> searchDishes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Double maxPrice) {

        List<ChatbotDishDTO> result = chatbotService.searchDishes(q, maxPrice);
        return ResponseEntity.ok(result);
    }

    /**
     * Retorna todos los platos activos de una categoría específica.
     *
     * <p>La comparación del nombre de categoría es insensible a mayúsculas/minúsculas.</p>
     *
     * @param categoryName nombre de la categoría a consultar
     * @return {@code 200 OK} con la lista de platos de la categoría indicada
     */
    @GetMapping("/categories/{categoryName}/dishes")
    public ResponseEntity<List<ChatbotDishDTO>> getDishesByCategory(
            @PathVariable String categoryName) {

        List<ChatbotDishDTO> result = chatbotService.getDishesByCategory(categoryName);
        return ResponseEntity.ok(result);
    }

    /**
     * Retorna los ingredientes activos de un plato dado su identificador.
     *
     * <p>Útil para que el chatbot valide alergias o restricciones alimentarias del cliente.</p>
     *
     * @param dishId identificador único del plato
     * @return {@code 200 OK} con el detalle del plato y sus ingredientes,
     *         o {@code 404 Not Found} si el plato no existe
     */
    @GetMapping("/dishes/{dishId}/ingredients")
    public ResponseEntity<ChatbotDishDetailDTO> getDishIngredients(
            @PathVariable String dishId) {

        ChatbotDishDetailDTO result = chatbotService.getDishIngredients(dishId);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }
}
