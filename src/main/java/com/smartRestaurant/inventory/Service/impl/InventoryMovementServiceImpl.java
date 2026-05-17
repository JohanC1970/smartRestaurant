package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.InventoryMovementRepository;
import com.smartRestaurant.inventory.Service.InventoryMovementService;
import com.smartRestaurant.inventory.dto.InventoryMovement.GetInventoryMovementDTO;
import com.smartRestaurant.inventory.mapper.InventoryMovementMapper;
import com.smartRestaurant.inventory.model.InventoryMovement;
import com.smartRestaurant.inventory.model.ItemCategory;
import com.smartRestaurant.inventory.model.Product;
import com.smartRestaurant.inventory.model.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import com.smartRestaurant.inventory.util.CurrentUserProvider;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryMovementServiceImpl implements InventoryMovementService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryMovementMapper inventoryMovementMapper;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    @Override
    public void registerMovementEntry(Product product, double weight, double unitPrice, String reason) {
        InventoryMovement movement = InventoryMovement.builder()
                .id(java.util.UUID.randomUUID().toString())
                .product(product)
                .user(currentUserProvider.getCurrentUser())
                .type(Type.ENTRY)
                .timeAt(LocalDateTime.now())
                .weight(weight)
                .unitPrice(unitPrice)
                .totalCost(weight * unitPrice)
                .reason(reason)
                .build();

        inventoryMovementRepository.save(movement);
    }

    @Transactional
    @Override
    public void registerMovementExit(Product product, double weight, String reason) {
        InventoryMovement movement = InventoryMovement.builder()
                .id(java.util.UUID.randomUUID().toString())
                .product(product)
                .user(currentUserProvider.getCurrentUser())
                .type(Type.EXIT)
                .timeAt(LocalDateTime.now())
                .weight(weight)
                .unitPrice(0.0)
                .totalCost(0.0)
                .reason(reason)
                .build();

        inventoryMovementRepository.save(movement);
    }

    @Transactional
    @Override
    public void registerDrinkEntry(String drinkId, String drinkName, int units, double purchasePrice) {
        InventoryMovement movement = InventoryMovement.builder()
                .id(java.util.UUID.randomUUID().toString())
                .product(null)
                .itemCategory(ItemCategory.DRINK)
                .itemId(drinkId)
                .itemName(drinkName)
                .user(currentUserProvider.getCurrentUser())
                .type(Type.ENTRY)
                .timeAt(LocalDateTime.now())
                .weight(units)
                .unitPrice(purchasePrice)
                .totalCost((double) units * purchasePrice)
                .reason("Reabastecimiento de bebida")
                .build();

        inventoryMovementRepository.save(movement);
    }

    @Transactional
    @Override
    public void registerAdditionEntry(String additionId, String additionName, int units, double purchasePrice) {
        InventoryMovement movement = InventoryMovement.builder()
                .id(java.util.UUID.randomUUID().toString())
                .product(null)
                .itemCategory(ItemCategory.ADDITION)
                .itemId(additionId)
                .itemName(additionName)
                .user(currentUserProvider.getCurrentUser())
                .type(Type.ENTRY)
                .timeAt(LocalDateTime.now())
                .weight(units)
                .unitPrice(purchasePrice)
                .totalCost((double) units * purchasePrice)
                .reason("Reabastecimiento de adición")
                .build();

        inventoryMovementRepository.save(movement);
    }

    @Override
    public List<GetInventoryMovementDTO> getAllMovements() {
        return inventoryMovementRepository.findAll().stream()
                .sorted((a, b) -> b.getTimeAt().compareTo(a.getTimeAt()))
                .map(inventoryMovementMapper::toDTO)
                .toList();
    }

}
