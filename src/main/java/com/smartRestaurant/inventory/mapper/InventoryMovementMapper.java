package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.InventoryMovement.GetInventoryMovementDTO;
import com.smartRestaurant.inventory.model.InventoryMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InventoryMovementMapper {

    @Mapping(target = "productId",   expression = "java(inventoryMovement.getProduct() != null ? inventoryMovement.getProduct().getId()   : inventoryMovement.getItemId())")
    @Mapping(target = "productName", expression = "java(inventoryMovement.getProduct() != null ? inventoryMovement.getProduct().getName() : inventoryMovement.getItemName())")
    @Mapping(target = "itemCategory", source = "itemCategory")
    @Mapping(target = "userName", expression = "java(inventoryMovement.getUser() != null ? inventoryMovement.getUser().getEmail() : \"Sistema\")")
    GetInventoryMovementDTO toDTO(InventoryMovement inventoryMovement);

}
