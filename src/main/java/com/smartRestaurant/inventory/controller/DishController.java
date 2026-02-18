package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.Service.DishService;
import com.smartRestaurant.inventory.dto.Dish.CreateDishDTO;
import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import com.smartRestaurant.inventory.dto.Dish.UpdateDishDTO;
import com.smartRestaurant.inventory.dto.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<GetDishDTO>>> getAll(){
        List<GetDishDTO> list = dishService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(list, false));
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO<String>> create(CreateDishDTO createDishDTO){
        dishService.create(createDishDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Dish Created", false));
    }

    @PutMapping("update")
    public ResponseEntity<ResponseDTO<String>> update(UpdateDishDTO updateDishDTO){
        dishService.update(updateDishDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Dish Updated", false));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable String id){
        dishService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Dish Deleted", false));
    }
}
