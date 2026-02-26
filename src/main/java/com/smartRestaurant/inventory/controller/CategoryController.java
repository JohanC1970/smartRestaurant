package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.Service.CategoryService;
import com.smartRestaurant.inventory.dto.Category.CreateCategoryDTO;
import com.smartRestaurant.inventory.dto.Category.GetCategoriesDTO;
import com.smartRestaurant.inventory.dto.Category.UpdateCategoryDTO;
import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import com.smartRestaurant.inventory.dto.ResponseDTO;
import com.smartRestaurant.inventory.dto.Suplier.GetSuplierDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/getAll")
    public ResponseEntity<ResponseDTO<List<GetCategoriesDTO>>> getAll(){

        List<GetCategoriesDTO> list = categoryService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(list, false));
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO<String>> create(@Valid @RequestBody CreateCategoryDTO createCategoryDTO) {
        categoryService.create(createCategoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>("Dish Created", false));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDTO<String>> update(@PathVariable String id, @Valid @RequestBody UpdateCategoryDTO updateCategoryDTO) {
        categoryService.update(id, updateCategoryDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Dish Updated", false));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable String id){
        categoryService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Dish Deleted", false));
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<ResponseDTO<GetCategoriesDTO>> getById(@PathVariable String id){
        GetCategoriesDTO categorieDTO = categoryService.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(categorieDTO, false));
    }

}
