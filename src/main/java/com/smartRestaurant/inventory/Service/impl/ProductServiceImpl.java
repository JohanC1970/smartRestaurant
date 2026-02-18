package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Service.CategoryService;
import com.smartRestaurant.inventory.Service.ProductService;
import com.smartRestaurant.inventory.dto.Product.CreateProductDTO;
import com.smartRestaurant.inventory.dto.Product.GetProductDTO;
import com.smartRestaurant.inventory.dto.Product.UpdateProductDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Override
    public void create(CreateProductDTO createProductDTO) {

    }

    @Override
    public void update(UpdateProductDTO updateProductDTO) {

    }

    @Override
    public void delete(String id) {

    }

    @Override
    public List<GetProductDTO> getAll() {
        return List.of();
    }
}
