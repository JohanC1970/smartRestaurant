package com.smartRestaurant.inventory.mapper;

import jakarta.persistence.ManyToOne;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DishMapper {
}
