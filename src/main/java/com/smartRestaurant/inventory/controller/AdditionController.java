package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.Service.AdditionService;
import com.smartRestaurant.inventory.Service.CategoryService;
import com.smartRestaurant.inventory.dto.Addition.CreateAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.UpdateAdditionDTO;
import com.smartRestaurant.inventory.dto.Category.CreateCategoryDTO;
import com.smartRestaurant.inventory.dto.Category.GetCategoriesDTO;
import com.smartRestaurant.inventory.dto.Category.UpdateCategoryDTO;
import com.smartRestaurant.inventory.dto.ResponseDTO;
import com.smartRestaurant.inventory.dto.Suplier.GetSuplierDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/additions")
@RequiredArgsConstructor
public class AdditionController {

    private final AdditionService additionService;

    @GetMapping("/{page}/getAll")
    public ResponseEntity<ResponseDTO<List<GetAdditionDTO>>> getAll(@PathVariable int page){

        List<GetAdditionDTO> list = additionService.getAll(page);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(list, false));
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO<String>> create(@Valid @RequestBody CreateAdditionDTO createAdditionDTO) {
        additionService.create(createAdditionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>("Addition Created", false));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDTO<String>> update(@PathVariable String id, @Valid @RequestBody UpdateAdditionDTO updateAdditionDTO) {
        additionService.update(id, updateAdditionDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Addition Updated", false));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable String id){
        additionService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Addition Deleted", false));
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<ResponseDTO<GetAdditionDTO>> getById(@PathVariable String id){
        GetAdditionDTO additionDTO = additionService.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(additionDTO, false));
    }
}
