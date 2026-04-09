package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.DishRepository;
import com.smartRestaurant.inventory.Repository.ProductRepository;
import com.smartRestaurant.inventory.Repository.RecipeRepository;
import com.smartRestaurant.inventory.Service.RecipeService;
import com.smartRestaurant.inventory.dto.recipe.CreateRecipeDTO;
import com.smartRestaurant.inventory.dto.recipe.GetRecipeDTO;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.mapper.RecipeMapper;
import com.smartRestaurant.inventory.model.Dish;
import com.smartRestaurant.inventory.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    private final ProductRepository productRepository;


    @Transactional
    @Override
    public void registerRecipe(List<CreateRecipeDTO> recipe, Dish dish) {

        for(CreateRecipeDTO createRecipeDTO : recipe) {

            Optional<Product> product = productRepository.findById(createRecipeDTO.product_id());
            if(product.isEmpty()) {
                throw new ResourceNotFoundException("Producto no encontrado");
            }
            recipeRepository.save(recipeMapper.toEntity(createRecipeDTO, dish, product.get()));
        }
    }

    @Override
    public List<GetRecipeDTO> getRecipesByDishID(String dishID) {

        return recipeRepository.findByDish_Id(dishID).stream()
                .map(recipeMapper::toDTO)
                .toList();
    }


}
