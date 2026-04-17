package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.Service.DishService;
import com.smartRestaurant.inventory.dto.Dish.CreateDishDTO;
import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import com.smartRestaurant.inventory.dto.Dish.GetDishDetailDTO;
import com.smartRestaurant.inventory.dto.Dish.UpdateDishDTO;
import com.smartRestaurant.inventory.dto.ResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @GetMapping("/{page}/page")
    @PreAuthorize("hasAnyAuthority('dish:read', 'ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_WAITER', 'ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<List<GetDishDTO>>> getAll(@PathVariable int page){
        List<GetDishDTO> list = dishService.getAll(page);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(list, false));
    }

    @PostMapping("/{categoryId}/categories")
    @PreAuthorize("hasAnyAuthority('dish:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> create(@PathVariable String categoryId, @RequestBody CreateDishDTO createDishDTO){
        dishService.create(categoryId, createDishDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Plato creado", false));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('dish:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> update(@PathVariable String id, @RequestBody @Valid UpdateDishDTO updateDishDTO){
        dishService.update(id, updateDishDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Plato actualizado", false));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('dish:delete', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable String id){
        dishService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Plato eliminado", false));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('dish:read', 'ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_WAITER', 'ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<GetDishDetailDTO>> getById(@PathVariable String id){
        GetDishDetailDTO dishDTO = dishService.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(dishDTO, false));
    }


}
