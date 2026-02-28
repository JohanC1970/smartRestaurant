package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.inventory.Repository.NotificationRepository;
import com.smartRestaurant.inventory.Repository.ProductRepository;
import com.smartRestaurant.inventory.Repository.SuplierRepository;
import com.smartRestaurant.inventory.Service.CategoryService;
import com.smartRestaurant.inventory.Service.InventoryMovementService;
import com.smartRestaurant.inventory.Service.ProductService;
import com.smartRestaurant.inventory.dto.Product.CreateProductDTO;
import com.smartRestaurant.inventory.dto.Product.GetProductDTO;
import com.smartRestaurant.inventory.dto.Product.StockMovementDTO;
import com.smartRestaurant.inventory.dto.Product.UpdateProductDTO;
import com.smartRestaurant.inventory.exceptions.BadRequestException;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.exceptions.ValueConflictException;
import com.smartRestaurant.inventory.mapper.ProductMapper;
import com.smartRestaurant.inventory.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final NotificationRepository notificationRepository;
    private final SuplierRepository suplierRepository;

    @Override
    public void create(String idSuplier,  CreateProductDTO createProductDTO) {

        Optional<Suplier> optionalSuplier = suplierRepository.findById(idSuplier);
        if(optionalSuplier.isEmpty() || optionalSuplier.get().getState().equals(State.INACTIVE)){
            throw new ResourceNotFoundException("El proveedor no existe");
        }

        Optional<Product> productOptional = productRepository.findByName(createProductDTO.name());
        if (productOptional.isPresent()) {
            throw new RuntimeException("El producto ya existe");
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

        productMapper.update(updateProductDTO, productOptional.get());
        productRepository.save(productOptional.get());

    }

    // hay que verificar primero si el producto está en algun carrito

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

    @Override
    public List<GetProductDTO> getAll(int page) {
        if(page < 0){
            throw new BadRequestException("pagina invalida (negativa), debe ser >= 0");
        }
        Pageable pageable = PageRequest.of(page,10);

        Page<Product> products = productRepository.findAll(pageable);
        if(products.getTotalElements() == 0){
            throw new ResourceNotFoundException("No existen productos encontrados");
        }

        return products.stream()
                .map(productMapper::toDTO)
                .toList();
    }

    @Override
    public void addStock(String id, StockMovementDTO stockMovementDTO) {

        Optional<Product> product = productRepository.findById(id);
        if(product.isEmpty() || product.get().getState().equals(State.INACTIVE)) {
            throw new ResourceNotFoundException("producto no encontrado");
        }

        double newWeight = product.get().getWeight() + stockMovementDTO.weight();
        product.get().setWeight(newWeight);
        productRepository.save(product.get());

        if(product.get().getWeight() < product.get().getMinimumStock()){

            Notification notification = Notification.builder()
                    .id("java.util.uuid.randomUUID().toString()")
                    .type("Bajo nivel de stock de: "+ product.get().getName())
                    .createdAt(LocalDateTime.now())
                    .description("Revisa el inventario "+ "el producto: "+product.get().getName()+ "ha llegado al stock minimo: "+product.get().getMinimumStock())
                    .build();

            notificationRepository.save(notification);
        }


        inventoryMovementService.registerMovementEntry(product.get(), stockMovementDTO.weight());
    }

    @Override
    public void discountStock(String id, StockMovementDTO stockMovementDTO) {

        Optional<Product> product = productRepository.findById(id);
        if(product.isEmpty()) {
            throw new ResourceNotFoundException("producto no encontrado");
        }
        double newWeight = product.get().getWeight() - stockMovementDTO.weight();

        if(newWeight < 0) {
            throw new ValueConflictException("No hay suficiente para descontar");
        }


        product.get().setWeight(newWeight);
        productRepository.save(product.get());

        if(product.get().getWeight() < product.get().getMinimumStock()){

            Notification notification = Notification.builder()
                    .id("java.util.uuid.randomUUID().toString()")
                    .type("Bajo nivel de stock de: "+ product.get().getName())
                    .createdAt(LocalDateTime.now())
                    .description("Revisa el inventario "+ "el producto: "+product.get().getName()+ "ha llegado al stock minimo: "+product.get().getMinimumStock())
                    .build();

            notificationRepository.save(notification);
        }

        inventoryMovementService.registerMovementExit(product.get(), stockMovementDTO.weight());

    }

    @Override
    public GetProductDTO getById(String id) {
        Optional<Product> product = productRepository.findById(id);
        if(product.isEmpty() || product.get().getState().equals(State.INACTIVE)) {
            throw new ResourceNotFoundException("producto no encontrado");
        }

        return productMapper.toDTO(product.get());
    }


    @Override
    public boolean posibleStock(List<Product> products) {
        if(products.isEmpty()) {
            return false;
        }
        for(Product product : products) {
            if(product.getWeight() > 0) {
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
