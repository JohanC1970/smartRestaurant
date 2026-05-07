package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.Dish.CreateDishDTO;
import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import com.smartRestaurant.inventory.dto.Dish.GetDishDetailDTO;
import com.smartRestaurant.inventory.dto.Dish.UpdateDishDTO;
import com.smartRestaurant.inventory.model.Dish;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = RecipeMapper.class)
public interface DishMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "state", constant = "ACTIVE")
    @Mapping(target = "photos", source = "photos")
    @Mapping(target = "availability", expression = "java(createDishDTO.availability() != null ? createDishDTO.availability() : com.smartRestaurant.inventory.model.DishAvailability.REGULAR)")
    Dish toEntity(CreateDishDTO createDishDTO);

    @Mapping(target = "categoryId", source = "dish.category.id")
    @Mapping(target = "categoryName", source = "dish.category.name")
    @Mapping(target = "ingredients", source = "recipes")
    @Mapping(target = "estimatedCost", expression = "java(calculateEstimatedCost(dish))")
    @Mapping(target = "margin", expression = "java(dish.getPrice() - calculateEstimatedCost(dish))")
    GetDishDetailDTO toDTO(Dish dish);

    @Mapping(target = "availability", expression = "java(updateDishDTO.availability() != null ? updateDishDTO.availability() : dish.getAvailability())")
    void updateDish(UpdateDishDTO updateDishDTO, @MappingTarget Dish dish);

    default double calculateEstimatedCost(Dish dish) {
        if (dish.getRecipes() == null) return 0.0;
        return dish.getRecipes().stream()
                .mapToDouble(r -> r.getWeight() * r.getProduct().getPrice())
                .sum();
    }
}
