package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.Service.ProductService;
import com.smartRestaurant.inventory.dto.Product.*;
import com.smartRestaurant.inventory.dto.ResponseDTO;
import com.smartRestaurant.inventory.dto.Suplier.GetSuplierDTO;
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

    @PostMapping("/{idSuplier}/supliers")
    public ResponseEntity<ResponseDTO<String>> create(@PathVariable String idSuplier, @RequestBody @Valid CreateProductDTO createProductDTO){
        productService.create(idSuplier, createProductDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>("Producto creado", false));

    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> update(@PathVariable String id, @RequestBody @Valid UpdateProductDTO updateProductDTO){
        productService.update(id, updateProductDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Producto actualizado", false));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable String id){
        productService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Producto eliminado", false));
    }

    @GetMapping("/{page}/page")
    public ResponseEntity<ResponseDTO<List<GetProductDTO>>> getAll(@PathVariable int page){
        List<GetProductDTO> list = productService.getAll(page);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(list, false));
    }

    @PatchMapping("/{id}/add")
    public ResponseEntity<ResponseDTO<String>> addStock(@PathVariable String id, @Valid @RequestBody StockMovementDTO stockMovementDTO){
        productService.addStock(id, stockMovementDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Stock añadido", false));
    }

    @PatchMapping("/{id}/discount")
    public ResponseEntity<ResponseDTO<String>> discountStock(@PathVariable String id, @Valid @RequestBody StockMovementDTO stockMovementDTO){
        productService.discountStock(id, stockMovementDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Stock descontado", false));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<GetProductDetailDTO>> getById(@PathVariable String id){
        GetProductDetailDTO productDTO = productService.getById(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(productDTO, false));
    }

}
