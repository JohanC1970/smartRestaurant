package com.smartRestaurant.chatbot.service.impl;

import com.smartRestaurant.chatbot.dto.response.ChatbotDishDTO;
import com.smartRestaurant.chatbot.dto.response.ChatbotDishDetailDTO;
import com.smartRestaurant.chatbot.repository.ChatbotDishRepository;
import com.smartRestaurant.chatbot.service.ChatbotService;
import com.smartRestaurant.inventory.model.Dish;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private final ChatbotDishRepository chatbotDishRepository;

    @Override
    public List<ChatbotDishDTO> searchDishes(String q, Double maxPrice) {
        return chatbotDishRepository.searchForChatbot(q, maxPrice)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatbotDishDTO> getDishesByCategory(String categoryName) {
        return chatbotDishRepository.findActiveByCategoryName(categoryName)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ChatbotDishDetailDTO getDishIngredients(String dishId) {
        return chatbotDishRepository.findById(dishId)
                .map(dish -> new ChatbotDishDetailDTO(
                        dish.getName(),
                        dish.getRecipes() != null ? dish.getRecipes().stream()
                                .map(recipe -> recipe.getProduct().getName())
                                .collect(Collectors.toList()) : List.of()))
                .orElse(null);
    }

    private ChatbotDishDTO mapToDTO(Dish dish) {
        return new ChatbotDishDTO(
                dish.getId(),
                dish.getName(),
                dish.getPrice(),
                dish.getDescription(),
                dish.getCategory() != null ? dish.getCategory().getName() : "General");
    }
}
