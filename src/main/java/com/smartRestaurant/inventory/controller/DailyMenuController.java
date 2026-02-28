package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.Service.DailyMenuService;
import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import com.smartRestaurant.inventory.dto.ResponseDTO;
import com.smartRestaurant.inventory.model.Dish;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dailyMenus")
@RequiredArgsConstructor
public class DailyMenuController {

    private final DailyMenuService dailyMenuService;

    @PostMapping("/add/{id}/dish")
    public ResponseEntity<ResponseDTO<String>> addDish(@PathVariable String id){
        dailyMenuService.add(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Dish added to daily menu", false));
    }

    @GetMapping("/{page}/all")
    public ResponseEntity<ResponseDTO<List<GetDishDTO>>> getAllDishes(@PathVariable int page){
        List<GetDishDTO> list = dailyMenuService.getAll(page);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(list, false));
    }

    @DeleteMapping("/delete/{id}/dish")
    public ResponseEntity<ResponseDTO<String>> deleteDish(@PathVariable String id){
        dailyMenuService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Dish deleted from daily menu", false));
    }
}
