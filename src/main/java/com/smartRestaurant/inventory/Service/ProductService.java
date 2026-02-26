package com.smartRestaurant.inventory.Service;

import com.smartRestaurant.inventory.dto.Product.CreateProductDTO;
import com.smartRestaurant.inventory.dto.Product.GetProductDTO;
import com.smartRestaurant.inventory.dto.Product.StockMovementDTO;
import com.smartRestaurant.inventory.dto.Product.UpdateProductDTO;
import com.smartRestaurant.inventory.model.Product;

import java.util.List;

public interface ProductService {

    void create(CreateProductDTO createProductDTO);
    void update(String id, UpdateProductDTO updateProductDTO);
    void delete(String id);
    List<GetProductDTO> getAll(int page);
    boolean posibleStock(List<Product> products);
    boolean calculateProduct(Product product, double weight);
    void addStock(String id, StockMovementDTO stockMovementDTO);
    void discountStock(String id, StockMovementDTO stockMovementDTO);
    GetProductDTO getById(String id);
}
