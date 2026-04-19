package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.Addition.GetAdditionDetailDTO;
import com.smartRestaurant.inventory.model.Addition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ShowAdditionDetailMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "photos", source = "photos")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "units", source = "units")
    @Mapping(target = "minimumStock", source = "minimumStock")

    GetAdditionDetailDTO toDTO(Addition addition);


}
