package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.InventoryMovementRepository;
import com.smartRestaurant.inventory.Service.InventoryMovementService;
import com.smartRestaurant.inventory.model.InventoryMovement;
import com.smartRestaurant.inventory.model.Product;
import com.smartRestaurant.inventory.model.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventoryMovementServiceImpl implements InventoryMovementService {

    private final InventoryMovementRepository inventoryMovementRepository;

    @Override
    public void registerMovementEntry(Product product, double weight) {

        InventoryMovement movement = InventoryMovement.builder()
                .id(java.util.UUID.randomUUID().toString())
                .productId(product.getId())
                //.user()
                .type(Type.ENTRY)
                .timeAt(LocalDateTime.now())
                .weight(product.getWeight())
                .reason("Entrada de producto por un total de: "+ weight + " gramos.")
                .build();

        inventoryMovementRepository.save(movement);
    }

    @Override
    public void registerMovementExit(Product product, double weight) {

        InventoryMovement movement = InventoryMovement.builder()
                .id(java.util.UUID.randomUUID().toString())
                .productId(product.getId())
                //.user()
                .type(Type.EXIT)
                .timeAt(LocalDateTime.now())
                .weight(product.getWeight())
                .reason("Salida de producto por un total de: "+ weight + " gramos.")
                .build();

        inventoryMovementRepository.save(movement);
    }

}
