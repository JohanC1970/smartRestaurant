package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.dto.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ProductController {

    public ResponseEntity<ResponseDTO<String>> create(){

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>("Dish Created", false));

    }

    public ResponseEntity<ResponseDTO<String>> update(){

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Dish Updated", false));
    }

    public ResponseEntity<ResponseDTO<String>> delete(){
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Dish Deleted", false));
    }

    public ResponseEntity<ResponseDTO<String>> getAll(){
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Dish List", false));
    }
}
