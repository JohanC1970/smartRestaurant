package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.InventoryMovement.GetInventoryMovementDTO;
import com.smartRestaurant.inventory.model.InventoryMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InventoryMovementMapper {

    @Mapping(source = "product.id",   target = "productId")
    @Mapping(source = "product.name", target = "productName")
    GetInventoryMovementDTO toDTO(InventoryMovement inventoryMovement);

}
