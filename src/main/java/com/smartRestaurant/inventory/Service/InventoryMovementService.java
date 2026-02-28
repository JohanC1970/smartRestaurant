package com.smartRestaurant.inventory.Service;

import com.smartRestaurant.inventory.model.Product;

public interface InventoryMovementService {

    void registerMovementEntry(Product product, double weight);
    void registerMovementExit(Product product, double weight);
}
