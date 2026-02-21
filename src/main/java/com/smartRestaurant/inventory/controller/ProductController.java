package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.Service.ProductService;
import com.smartRestaurant.inventory.dto.Product.CreateProductDTO;
import com.smartRestaurant.inventory.dto.Product.GetProductDTO;
import com.smartRestaurant.inventory.dto.Product.StockMovementDTO;
import com.smartRestaurant.inventory.dto.Product.UpdateProductDTO;
import com.smartRestaurant.inventory.dto.ResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO<String>> create(@RequestBody @Valid CreateProductDTO createProductDTO){
        productService.create(createProductDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>("Product Created", false));

    }

    @PutMapping("/{id}/update")
    public ResponseEntity<ResponseDTO<String>> update(@PathVariable String id, @RequestBody @Valid UpdateProductDTO updateProductDTO){
        productService.update(id, updateProductDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Product Updated", false));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable String id){
        productService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Product Deleted", false));
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<GetProductDTO>>> getAll(){
        List<GetProductDTO> list = productService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(list, false));
    }

    @PatchMapping("/{id}/add")
    public ResponseEntity<ResponseDTO<String>> addStock(@PathVariable String id, @Valid @RequestBody StockMovementDTO stockMovementDTO){
        productService.addStock(id, stockMovementDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Stock Added", false));
    }

    @PatchMapping("/{id}/discount")
    public ResponseEntity<ResponseDTO<String>> discountStock(@PathVariable String id, @Valid @RequestBody StockMovementDTO stockMovementDTO){
        productService.discountStock(id, stockMovementDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Stock discounted", false));
    }

}
