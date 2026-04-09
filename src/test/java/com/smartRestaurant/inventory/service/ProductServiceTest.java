package com.smartRestaurant.inventory.service;

import com.smartRestaurant.inventory.Repository.NotificationRepository;
import com.smartRestaurant.inventory.Repository.ProductRepository;
import com.smartRestaurant.inventory.Repository.SuplierRepository;
import com.smartRestaurant.inventory.Service.InventoryMovementService;
import com.smartRestaurant.inventory.Service.impl.ProductServiceImpl;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.mapper.ProductMapper;
import com.smartRestaurant.inventory.model.Product;
import com.smartRestaurant.inventory.model.State;
import com.smartRestaurant.inventory.model.Suplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private InventoryMovementService inventoryMovementService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SuplierRepository suplierRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private Suplier testSuplier;

    @BeforeEach
    void setUp() {
        testSuplier = new Suplier();
        testSuplier.setId("suplier-1");
        testSuplier.setName("Proveedor Test");
        testSuplier.setState(State.ACTIVE);

        testProduct = new Product();
        testProduct.setId("product-1");
        testProduct.setName("Arroz");
        testProduct.setWeight(100.0);
        testProduct.setMinimumStock(20.0);
        testProduct.setState(State.ACTIVE);
        testProduct.setSuplier(testSuplier);
    }

    @Test
    void update_WithValidData_UpdatesProduct() {
        // Arrange
        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act & Assert
        assertDoesNotThrow(() -> productService.update("product-1", null));
        verify(productRepository).save(testProduct);
    }

    @Test
    void update_WithNonExistentProduct_ThrowsException() {
        // Arrange
        when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> productService.update("invalid-id", null));
    }

    @Test
    void delete_WithValidId_SetsProductInactive() {
        // Arrange
        when(productRepository.existsById("product-1")).thenReturn(true);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        productService.delete("product-1");

        // Assert
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void delete_WithNonExistentProduct_ThrowsException() {
        // Arrange
        when(productRepository.existsById("invalid-id")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> productService.delete("invalid-id"));
    }

    @Test
    void getById_WithValidId_ReturnsProduct() {
        // Arrange
        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertDoesNotThrow(() -> productService.getById("product-1"));
    }

    @Test
    void getById_WithNonExistentProduct_ThrowsException() {
        // Arrange
        when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.getById("invalid-id"));
    }
}
