package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.Suplier.CreateSuplierDTO;
import com.smartRestaurant.inventory.dto.Suplier.GetSuplierDTO;
import com.smartRestaurant.inventory.dto.Suplier.UpdateSuplierDTO;
import com.smartRestaurant.inventory.model.Suplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SuplierMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "state", constant = "ACTIVE")

    Suplier toEntity(CreateSuplierDTO createSuplierDTO);
    GetSuplierDTO toDto(Suplier suplier);


    void updateDto(UpdateSuplierDTO dto, @MappingTarget Suplier suplier);




}
