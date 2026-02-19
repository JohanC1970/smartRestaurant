package com.smartRestaurant.inventory.Service;

import com.smartRestaurant.inventory.dto.Product.CreateProductDTO;
import com.smartRestaurant.inventory.dto.Product.GetProductDTO;
import com.smartRestaurant.inventory.dto.Product.UpdateProductDTO;

import java.util.List;

public interface ProductService {

    void create(CreateProductDTO createProductDTO);
    void update(String id, UpdateProductDTO updateProductDTO);
    void delete(String id);
    List<GetProductDTO> getAll();
}
