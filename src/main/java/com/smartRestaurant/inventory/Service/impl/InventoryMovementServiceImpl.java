package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.InventoryMovementRepository;
import com.smartRestaurant.inventory.Service.InventoryMovementService;
import com.smartRestaurant.inventory.dto.InventoryMovement.GetInventoryMovementDTO;
import com.smartRestaurant.inventory.mapper.InventoryMovementMapper;
import com.smartRestaurant.inventory.model.InventoryMovement;
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
    public void registerMovementEntry(Product product, double weight) {

        InventoryMovement movement = InventoryMovement.builder()
                .id(java.util.UUID.randomUUID().toString())
                .productId(product.getId())
                .user(currentUserProvider.getCurrentUser())
                .type(Type.ENTRY)
                .timeAt(LocalDateTime.now())
                .weight(product.getWeight())
                .reason("Entrada de producto por un total de: "+ weight + " gramos.")
                .build();

        inventoryMovementRepository.save(movement);
    }

    @Transactional
    @Override
    public void registerMovementExit(Product product, double weight) {

        InventoryMovement movement = InventoryMovement.builder()
                .id(java.util.UUID.randomUUID().toString())
                .productId(product.getId())
                .user(currentUserProvider.getCurrentUser())
                .type(Type.EXIT)
                .timeAt(LocalDateTime.now())
                .weight(product.getWeight())
                .reason("Salida de producto por un total de: "+ weight + " gramos.")
                .build();

        inventoryMovementRepository.save(movement);
    }

    @Override
    public List<GetInventoryMovementDTO> getAllMovements() {
        return inventoryMovementRepository.findAll().stream()
                .map(inventoryMovementMapper::toDTO)
                .toList();
    }

}
