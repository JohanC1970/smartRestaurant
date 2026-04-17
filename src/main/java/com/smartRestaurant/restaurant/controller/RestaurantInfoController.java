package com.smartRestaurant.restaurant.controller;

import com.smartRestaurant.orders.dto.ResponseDTO;
import com.smartRestaurant.restaurant.dto.RestaurantInfoDTO;
import com.smartRestaurant.restaurant.dto.UpdateRestaurantInfoDTO;
import com.smartRestaurant.restaurant.service.RestaurantInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
public class RestaurantInfoController {

    private final RestaurantInfoService service;

    /** Público — usado por la landing y el módulo de pedidos */
    @GetMapping
    public ResponseEntity<ResponseDTO<RestaurantInfoDTO>> get() {
        return ResponseEntity.ok(new ResponseDTO<>(service.get(), false));
    }

    /** Público — para validar si el restaurante está abierto antes de crear un pedido */
    @GetMapping("/is-open")
    public ResponseEntity<ResponseDTO<Boolean>> isOpen() {
        return ResponseEntity.ok(new ResponseDTO<>(service.isOpen(), false));
    }

    /** Solo ADMIN puede actualizar la información */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<RestaurantInfoDTO>> update(@RequestBody @Valid UpdateRestaurantInfoDTO dto) {
        return ResponseEntity.ok(new ResponseDTO<>(service.update(dto), false));
    }
}
