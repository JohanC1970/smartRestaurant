package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import com.smartRestaurant.inventory.model.Dish;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ShowDishesMappper {

    // Para el listado (resumen)
    @Mapping(target = "photo", expression = "java(dish.getPhotos() != null && !dish.getPhotos().isEmpty() ? dish.getPhotos().get(0) : null)")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "price", source = "price")
    @Mapping(source = "id", target = "id")

    GetDishDTO toDTO(Dish dish);

}
