package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.Service.DrinkService;
import com.smartRestaurant.inventory.dto.ResponseDTO;
import com.smartRestaurant.inventory.dto.Suplier.GetSuplierDTO;
import com.smartRestaurant.inventory.dto.drink.CreateDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.UpdateDrinkDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drinks")
@RequiredArgsConstructor
public class DrinkController {


    private final DrinkService drinkService;

    @GetMapping("/{page}/getAll")
    public ResponseEntity<ResponseDTO<List<GetDrinkDTO>>> getAll(@PathVariable int page){

        List<GetDrinkDTO> list = drinkService.getAll(page);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(list, false));
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO<String>> create(@Valid @RequestBody CreateDrinkDTO createDrinkDTO) {
        drinkService.create(createDrinkDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>("Drink Created", false));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDTO<String>> update(@PathVariable String id, @Valid @RequestBody UpdateDrinkDTO updateDrinkDTO) {
        drinkService.update(id, updateDrinkDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Drink Updated", false));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable String id){
        drinkService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Drink Deleted", false));
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<ResponseDTO<GetDrinkDTO>> getById(@PathVariable String id){
        GetDrinkDTO drink = drinkService.getDrinkById(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(drink, false));
    }
}
