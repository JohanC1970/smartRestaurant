package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.recipe.CreateRecipeDTO;
import com.smartRestaurant.inventory.dto.recipe.GetRecipeDTO;
import com.smartRestaurant.inventory.model.Dish;
import com.smartRestaurant.inventory.model.Product;
import com.smartRestaurant.inventory.model.Recipe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RecipeMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "dish", source = "dish")
    @Mapping(target = "product", source = "product")
    @Mapping(target = "product_id", source = "recipe.product.id")
    @Mapping(target = "product_name", source = "recipe.product.name")


    Recipe toEntity(CreateRecipeDTO dto, Dish dish, Product product);

    GetRecipeDTO toDTO(Recipe recipe);
}
