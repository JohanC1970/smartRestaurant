package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.InventoryMovement.GetInventoryMovementDTO;
import com.smartRestaurant.inventory.model.InventoryMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InventoryMovementMapper {

    @Mapping(source = "productId", target = "productId")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "weight", target = "weight")
    @Mapping(source = "timeAt", target = "timeAt")
    @Mapping(source = "reason", target = "reason")

    InventoryMovement toEntity(GetInventoryMovementDTO dto);
    @Mapping(source = "productId", target = "productId")

    GetInventoryMovementDTO toDTO(InventoryMovement inventoryMovement);

}
