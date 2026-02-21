package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.inventory.Repository.ProductRepository;
import com.smartRestaurant.inventory.Service.CategoryService;
import com.smartRestaurant.inventory.Service.InventoryMovementService;
import com.smartRestaurant.inventory.Service.ProductService;
import com.smartRestaurant.inventory.dto.Product.CreateProductDTO;
import com.smartRestaurant.inventory.dto.Product.GetProductDTO;
import com.smartRestaurant.inventory.dto.Product.UpdateProductDTO;
import com.smartRestaurant.inventory.mapper.ProductMapper;
import com.smartRestaurant.inventory.model.InventoryMovement;
import com.smartRestaurant.inventory.model.Product;
import com.smartRestaurant.inventory.model.State;
import com.smartRestaurant.inventory.model.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final InventoryMovementService inventoryMovementService;

    @Override
    public void create(CreateProductDTO createProductDTO) {

        Optional<Product> productOptional = productRepository.findByName(createProductDTO.name());
        if (productOptional.isPresent()) {
            throw new RuntimeException("Product already exists");
        }

        Product product = productMapper.toEntity(createProductDTO);
        productRepository.save(product);
        // registramos el movimiento
        inventoryMovementService.registerMovementEntry(product, createProductDTO.weight());

    }

    @Override
    public void update(String id, UpdateProductDTO updateProductDTO) {

        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) {
            throw new RuntimeException("Product not found");
        }

        double Oldweight = productOptional.get().getWeight();
        double newWeight = updateProductDTO.weight();
        double difference = newWeight - Oldweight;

        productMapper.update(updateProductDTO, productOptional.get());
        productRepository.save(productOptional.get());


        if(difference < 0){

            inventoryMovementService.registerMovementExit(productOptional.get(), difference);
        }else {

            inventoryMovementService.registerMovementEntry(productOptional.get(), difference);
        }

    }

    @Override
    public void delete(String id) {
        boolean found = productRepository.existsById(id);
        if (!found) {
            throw new RuntimeException("Product not found");
        }

        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty() ||  productOptional.get().getState().equals(State.INACTIVE)) {
            throw new RuntimeException("Product not found");
        }
        productOptional.get().setState(State.INACTIVE);
        productRepository.save(productOptional.get());

    }

    // Me falta la paginación
    @Override
    public List<GetProductDTO> getAll() {
        return productRepository.getAll().stream().map(productMapper::toDTO).toList();
    }

    @Override
    public boolean posibleStock(List<Product> products) {
        if(products.isEmpty()) {
            return false;
        }
        for(Product product : products) {
            if(product.getStock() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean calculateProduct(Product product, double weight) {
        return false;
    }
}
