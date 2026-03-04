package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.Service.InventoryMovementService;
import com.smartRestaurant.inventory.dto.InventoryMovement.GetInventoryMovementDTO;
import com.smartRestaurant.inventory.dto.ResponseDTO;
import com.smartRestaurant.inventory.mapper.InventoryMovementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryMovementController {

    private final InventoryMovementService inventoryMovementService;


    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<GetInventoryMovementDTO>>> getAllMovements() {
        List<GetInventoryMovementDTO> movements = inventoryMovementService.getAllMovements();
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(movements, false));
    }
}
