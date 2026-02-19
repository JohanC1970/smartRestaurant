package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.Dish.CreateDishDTO;
import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import com.smartRestaurant.inventory.dto.Dish.UpdateDishDTO;
import com.smartRestaurant.inventory.model.Dish;
import jakarta.persistence.ManyToOne;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DishMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUUID().toString())")

    Dish toEntity(CreateDishDTO createDishDTO);
    GetDishDTO toDTO(Dish dish);


    // atributos que se actualizan ...
    void updateDish(UpdateDishDTO updateDishDTO, @MappingTarget Dish dish);
}
