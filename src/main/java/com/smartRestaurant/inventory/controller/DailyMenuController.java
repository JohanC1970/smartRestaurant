package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.Service.DailyMenuService;
import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import com.smartRestaurant.inventory.dto.ResponseDTO;
import com.smartRestaurant.inventory.model.Dish;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dailyMenus")
@RequiredArgsConstructor
public class DailyMenuController {

    private final DailyMenuService dailyMenuService;

    @PostMapping("/{id}/dishes")
    @PreAuthorize("hasAnyAuthority('daily_menu:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> addDish(@PathVariable String id){
        dailyMenuService.add(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Plato añadido al menu diario", false));
    }

    @GetMapping("/{page}/page")
    @PreAuthorize("hasAnyAuthority('daily_menu:read', 'ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_WAITER')")
    public ResponseEntity<ResponseDTO<List<GetDishDTO>>> getAllDishes(@PathVariable int page){
        List<GetDishDTO> list = dailyMenuService.getAll(page);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(list, false));
    }

    @DeleteMapping("{id}/dishes")
    @PreAuthorize("hasAnyAuthority('daily_menu:delete', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> deleteDish(@PathVariable String id){
        dailyMenuService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Plato eliminado del menu diario", false));
    }
}
