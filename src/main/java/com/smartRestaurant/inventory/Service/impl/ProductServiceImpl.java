package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.ProductRepository;
import com.smartRestaurant.inventory.Service.CategoryService;
import com.smartRestaurant.inventory.Service.ProductService;
import com.smartRestaurant.inventory.dto.Product.CreateProductDTO;
import com.smartRestaurant.inventory.dto.Product.GetProductDTO;
import com.smartRestaurant.inventory.dto.Product.UpdateProductDTO;
import com.smartRestaurant.inventory.mapper.ProductMapper;
import com.smartRestaurant.inventory.model.Product;
import com.smartRestaurant.inventory.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public void create(CreateProductDTO createProductDTO) {

        Optional<Product> productOptional = productRepository.findByName(createProductDTO.name());
        if (productOptional.isPresent()) {
            throw new RuntimeException("Product already exists");
        }

        productRepository.save(productMapper.toEntity(createProductDTO));

    }

    @Override
    public void update(String id, UpdateProductDTO updateProductDTO) {

        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) {
            throw new RuntimeException("Product not found");
        }

        productMapper.update(updateProductDTO, productOptional.get());
        productRepository.save(productOptional.get());
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
}
