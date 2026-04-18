package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.Service.AdditionService;
import com.smartRestaurant.inventory.Service.CategoryService;
import com.smartRestaurant.inventory.dto.Addition.CreateAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDetailDTO;
import com.smartRestaurant.inventory.dto.Addition.UpdateAdditionDTO;
import com.smartRestaurant.inventory.dto.Category.CreateCategoryDTO;
import com.smartRestaurant.inventory.dto.Category.GetCategoriesDTO;
import com.smartRestaurant.inventory.dto.Category.UpdateCategoryDTO;
import com.smartRestaurant.inventory.dto.ResponseDTO;
import com.smartRestaurant.inventory.dto.Suplier.GetSuplierDTO;
import com.smartRestaurant.inventory.dto.drink.DrinkMovement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/additions")
@RequiredArgsConstructor
public class AdditionController {

    private final AdditionService additionService;

    // pagination
    @GetMapping("/{page}/page")
    @PreAuthorize("hasAnyAuthority('addition:read', 'ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_WAITER', 'ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<List<GetAdditionDTO>>> getAll(@PathVariable int page){
        List<GetAdditionDTO> list = additionService.getAll(page);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(list, false));
    }

    @PostMapping()
    @PreAuthorize("hasAnyAuthority('addition:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> create(@Valid @RequestBody CreateAdditionDTO createAdditionDTO) {
        additionService.create(createAdditionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>("Adicion creada", false));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('addition:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> update(@PathVariable String id, @Valid @RequestBody UpdateAdditionDTO updateAdditionDTO) {
        additionService.update(id, updateAdditionDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Adicion actualizada", false));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('addition:delete', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable String id){
        additionService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Adicion eliminada", false));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('addition:read', 'ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_WAITER', 'ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<GetAdditionDetailDTO>> getById(@PathVariable String id){
        GetAdditionDetailDTO additionDTO = additionService.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(additionDTO, false));
    }

    @PatchMapping("/{id}/add")
    @PreAuthorize("hasAnyAuthority('addition:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> addStock(@PathVariable String id, @Valid @RequestBody DrinkMovement movement) {
        additionService.addStock(id, movement);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Stock de adición añadido", false));
    }

    @PatchMapping("/{id}/discount")
    @PreAuthorize("hasAnyAuthority('addition:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> discountStock(@PathVariable String id, @Valid @RequestBody DrinkMovement movement) {
        additionService.discountStock(id, movement);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Stock de adición descontado", false));
    }
}
