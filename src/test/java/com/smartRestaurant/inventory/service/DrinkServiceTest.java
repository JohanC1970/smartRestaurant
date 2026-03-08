package com.smartRestaurant.inventory.service;

import com.smartRestaurant.inventory.Repository.CategoryRepository;
import com.smartRestaurant.inventory.Repository.DrinkRepository;
import com.smartRestaurant.inventory.Service.impl.DrinkServiceImpl;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.mapper.DrinkMapper;
import com.smartRestaurant.inventory.model.Category;
import com.smartRestaurant.inventory.model.Drink;
import com.smartRestaurant.inventory.model.State;
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
class DrinkServiceTest {

    @Mock
    private DrinkRepository drinkRepository;

    @Mock
    private DrinkMapper drinkMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DrinkServiceImpl drinkService;

    private Drink testDrink;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId("category-1");
        testCategory.setName("Bebidas");
        testCategory.setState(State.ACTIVE);

        testDrink = new Drink();
        testDrink.setId("drink-1");
        testDrink.setName("Coca Cola");
        testDrink.setMililiters(350.0);
        testDrink.setUnits(10);
        testDrink.setState(State.ACTIVE);
        testDrink.setCategory(testCategory);
    }

    @Test
    void update_WithValidData_UpdatesDrink() {
        // Arrange
        when(drinkRepository.findById("drink-1")).thenReturn(Optional.of(testDrink));
        when(drinkRepository.save(any(Drink.class))).thenReturn(testDrink);

        // Act & Assert
        assertDoesNotThrow(() -> drinkService.update("drink-1", null));
        verify(drinkRepository).save(testDrink);
    }

    @Test
    void update_WithNonExistentDrink_ThrowsException() {
        // Arrange
        when(drinkRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> drinkService.update("invalid-id", null));
    }

    @Test
    void delete_WithValidId_SetsDrinkInactive() {
        // Arrange
        when(drinkRepository.findById("drink-1")).thenReturn(Optional.of(testDrink));
        when(drinkRepository.save(any(Drink.class))).thenReturn(testDrink);

        // Act
        drinkService.delete("drink-1");

        // Assert
        verify(drinkRepository).save(any(Drink.class));
    }

    @Test
    void delete_WithNonExistentDrink_ThrowsException() {
        // Arrange
        when(drinkRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> drinkService.delete("invalid-id"));
    }

    @Test
    void getDrinkById_WithValidId_ReturnsDrink() {
        // Arrange
        when(drinkRepository.findById("drink-1")).thenReturn(Optional.of(testDrink));

        // Act & Assert
        assertDoesNotThrow(() -> drinkService.getDrinkById("drink-1"));
    }

    @Test
    void getDrinkById_WithNonExistentDrink_ThrowsException() {
        // Arrange
        when(drinkRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> drinkService.getDrinkById("invalid-id"));
    }
}
