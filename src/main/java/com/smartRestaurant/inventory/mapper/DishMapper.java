package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.Dish.CreateDishDTO;
import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import com.smartRestaurant.inventory.dto.Dish.GetDishDetailDTO;
import com.smartRestaurant.inventory.dto.Dish.UpdateDishDTO;
import com.smartRestaurant.inventory.model.Dish;
import jakarta.persistence.ManyToOne;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = RecipeMapper.class)
public interface DishMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "state", constant = "ACTIVE")
    @Mapping(target = "photos", source = "photos")

    Dish toEntity(CreateDishDTO createDishDTO);

    @Mapping(target = "categoryName", source = "dish.category.name")
    @Mapping(target = "ingredients", source = "recipes")
    GetDishDetailDTO toDTO(Dish dish);

    void updateDish(UpdateDishDTO updateDishDTO, @MappingTarget Dish dish);
}
