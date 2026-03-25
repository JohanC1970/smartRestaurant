package com.smartRestaurant.inventory.service;

import com.smartRestaurant.inventory.Repository.CategoryRepository;
import com.smartRestaurant.inventory.Repository.DishRepository;
import com.smartRestaurant.inventory.Repository.RecipeRepository;
import com.smartRestaurant.inventory.Service.RecipeService;
import com.smartRestaurant.inventory.Service.impl.DishServiceImpl;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.mapper.DishMapper;
import com.smartRestaurant.inventory.mapper.ShowDishesMappper;
import com.smartRestaurant.inventory.model.Category;
import com.smartRestaurant.inventory.model.Dish;
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
class DishServiceTest {

    @Mock
    private DishRepository dishRepository;

    @Mock
    private DishMapper dishMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RecipeService recipeService;

    @Mock
    private ShowDishesMappper showDishesMappper;

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private DishServiceImpl dishService;

    private Dish testDish;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId("category-1");
        testCategory.setName("Platos Principales");
        testCategory.setState(State.ACTIVE);

        testDish = new Dish();
        testDish.setId("dish-1");
        testDish.setName("Bandeja Paisa");
        testDish.setPrice(25000);
        testDish.setState(State.ACTIVE);
        testDish.setCategory(testCategory);
    }

    @Test
    void update_WithValidData_UpdatesDish() {
        // Arrange
        when(dishRepository.findById("dish-1")).thenReturn(Optional.of(testDish));
        when(dishRepository.save(any(Dish.class))).thenReturn(testDish);

        // Act & Assert
        assertDoesNotThrow(() -> dishService.update("dish-1", null));
        verify(dishRepository).save(testDish);
    }

    @Test
    void update_WithNonExistentDish_ThrowsException() {
        // Arrange
        when(dishRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> dishService.update("invalid-id", null));
    }

    @Test
    void delete_WithValidId_SetsDishInactive() {
        // Arrange
        when(dishRepository.findById("dish-1")).thenReturn(Optional.of(testDish));
        when(dishRepository.save(any(Dish.class))).thenReturn(testDish);

        // Act
        dishService.delete("dish-1");

        // Assert
        verify(recipeRepository).updateStateByDishId("dish-1", State.INACTIVE);
        verify(dishRepository).save(any(Dish.class));
    }

    @Test
    void delete_WithNonExistentDish_ThrowsException() {
        // Arrange
        when(dishRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> dishService.delete("invalid-id"));
    }

    @Test
    void getById_WithValidId_ReturnsDish() {
        // Arrange
        when(dishRepository.findById("dish-1")).thenReturn(Optional.of(testDish));

        // Act & Assert
        assertDoesNotThrow(() -> dishService.getById("dish-1"));
    }

    @Test
    void getById_WithNonExistentDish_ThrowsException() {
        // Arrange
        when(dishRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> dishService.getById("invalid-id"));
    }
}
