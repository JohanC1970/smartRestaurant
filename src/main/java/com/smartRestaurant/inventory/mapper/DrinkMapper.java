package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.drink.CreateDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.UpdateDrinkDTO;
import com.smartRestaurant.inventory.model.Drink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DrinkMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "state", constant = "ACTIVE")
    @Mapping(target = "photos", source = "photos")

    Drink toEntity(CreateDrinkDTO createDrinkDTO);
    GetDrinkDTO toDTO(Drink drink);


    // demas atributos
    void update(UpdateDrinkDTO updateDrinkDTO, @MappingTarget Drink drink);
}
