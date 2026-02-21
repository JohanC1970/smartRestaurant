package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.model.DailyMenu;
import com.smartRestaurant.inventory.model.Dish;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DailyMenuMapper {

    @Mapping(target = "id", expression = "java(java.util.uuid.randomUUID().toString())")
    @Mapping(source = "dish", target = "dish")

    DailyMenu toEntity(Dish dish);

}
