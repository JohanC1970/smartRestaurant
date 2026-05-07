package com.smartRestaurant.inventory.Service;

import com.smartRestaurant.inventory.dto.InventoryMovement.GetInventoryMovementDTO;
import com.smartRestaurant.inventory.model.InventoryMovement;
import com.smartRestaurant.inventory.model.Product;

import java.util.List;

public interface InventoryMovementService {

    void registerMovementEntry(Product product, double weight, double unitPrice, String reason);
    void registerMovementExit(Product product, double weight, String reason);
    void registerDrinkEntry(String drinkId, String drinkName, int units, double purchasePrice);
    void registerAdditionEntry(String additionId, String additionName, int units, double purchasePrice);
    List<GetInventoryMovementDTO> getAllMovements();
}
