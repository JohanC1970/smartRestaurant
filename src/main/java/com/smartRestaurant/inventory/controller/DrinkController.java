package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.Service.DrinkService;
import com.smartRestaurant.inventory.dto.ResponseDTO;
import com.smartRestaurant.inventory.dto.drink.CreateDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.DrinkMovement;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDetailDTO;
import com.smartRestaurant.inventory.dto.drink.UpdateDrinkDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drinks")
@RequiredArgsConstructor
public class DrinkController {

    private final DrinkService drinkService;

    @GetMapping("/{page}/page")
    @PreAuthorize("hasAnyAuthority('drink:read', 'ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_WAITER', 'ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<List<GetDrinkDTO>>> getAll(@PathVariable int page){
        List<GetDrinkDTO> list = drinkService.getAll(page);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(list, false));
    }

    @PostMapping("/{categorieId}/categories")
    @PreAuthorize("hasAnyAuthority('drink:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> create(@PathVariable String categorieId, @Valid @RequestBody CreateDrinkDTO createDrinkDTO) {
        drinkService.create(categorieId, createDrinkDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>("Bebida creada", false));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('drink:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> update(@PathVariable String id, @Valid @RequestBody UpdateDrinkDTO updateDrinkDTO) {
        drinkService.update(id, updateDrinkDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Bebida actualizada", false));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('drink:delete', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable String id){
        drinkService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Bebida eliminada", false));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('drink:read', 'ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_WAITER', 'ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<GetDrinkDetailDTO>> getById(@PathVariable String id){
        GetDrinkDetailDTO drink = drinkService.getDrinkById(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(drink, false));
    }

    @PatchMapping("/{id}/add")
    @PreAuthorize("hasAnyAuthority('drink:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> addStock(@PathVariable String id, @Valid @RequestBody DrinkMovement drinkMovement) {
        drinkService.addStock(id, drinkMovement);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Stock de bebida añadido", false));
    }

    @PatchMapping("/{id}/discount")
    @PreAuthorize("hasAnyAuthority('drink:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> discountStock(@PathVariable String id, @Valid @RequestBody DrinkMovement drinkMovement) {
        drinkService.discountStock(id, drinkMovement);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Stock de bebida descontado", false));
    }
}
