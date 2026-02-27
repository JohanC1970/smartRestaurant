package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.Addition.CreateAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.UpdateAdditionDTO;
import com.smartRestaurant.inventory.dto.drink.CreateDrinkDTO;
import com.smartRestaurant.inventory.model.Addition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdditionMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    Addition toEntity(CreateAdditionDTO createAdditionDTO);
    GetAdditionDTO toDto(Addition addition);

    // atributos a mapear en la actualizada
    void update(UpdateAdditionDTO updateAdditionDTO, @MappingTarget Addition addition);
}
