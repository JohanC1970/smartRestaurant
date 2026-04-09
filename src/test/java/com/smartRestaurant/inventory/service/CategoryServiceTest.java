package com.smartRestaurant.inventory.service;

import com.smartRestaurant.inventory.Repository.CategoryRepository;
import com.smartRestaurant.inventory.Service.impl.CategoryServiceImpl;
import com.smartRestaurant.inventory.dto.Category.CreateCategoryDTO;
import com.smartRestaurant.inventory.dto.Category.GetCategoriesDTO;
import com.smartRestaurant.inventory.dto.Category.UpdateCategoryDTO;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.mapper.CategoryMapper;
import com.smartRestaurant.inventory.model.Category;
import com.smartRestaurant.inventory.model.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId("category-1");
        testCategory.setName("Entradas");
        testCategory.setDescription("Platos de entrada");
        testCategory.setState(State.ACTIVE);
    }

    @Test
    void getAll_ReturnsActiveCategories() {
        // Arrange
        GetCategoriesDTO categoryDTO = new GetCategoriesDTO("category-1", "Entradas", "Platos de entrada");
        
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(testCategory));
        when(categoryMapper.toDTO(testCategory)).thenReturn(categoryDTO);

        // Act
        List<GetCategoriesDTO> result = categoryService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Entradas", result.get(0).name());
        verify(categoryRepository).findAll();
    }

    @Test
    void create_WithValidData_CreatesCategory() {
        // Arrange
        CreateCategoryDTO createDTO = new CreateCategoryDTO("Postres", "Platos dulces");
        
        when(categoryRepository.findByName("Postres")).thenReturn(Optional.empty());
        when(categoryMapper.toEntity(createDTO)).thenReturn(testCategory);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        categoryService.create(createDTO);

        // Assert
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_WithExistingCategory_ThrowsException() {
        // Arrange
        CreateCategoryDTO createDTO = new CreateCategoryDTO("Entradas", "Platos de entrada");
        
        when(categoryRepository.findByName("Entradas")).thenReturn(Optional.of(testCategory));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> categoryService.create(createDTO));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void update_WithValidData_UpdatesCategory() {
        // Arrange
        UpdateCategoryDTO updateDTO = new UpdateCategoryDTO("Entradas Especiales", "Platos de entrada premium");
        
        when(categoryRepository.findById("category-1")).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        categoryService.update("category-1", updateDTO);

        // Assert
        verify(categoryMapper).update(updateDTO, testCategory);
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void delete_WithValidId_SetsCategoryInactive() {
        // Arrange
        when(categoryRepository.findById("category-1")).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        categoryService.delete("category-1");

        // Assert
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void getCategoryById_WithValidId_ReturnsCategory() {
        // Arrange
        GetCategoriesDTO categoryDTO = new GetCategoriesDTO("category-1", "Entradas", "Platos de entrada");
        
        when(categoryRepository.findById("category-1")).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toDTO(testCategory)).thenReturn(categoryDTO);

        // Act
        GetCategoriesDTO result = categoryService.getCategoryById("category-1");

        // Assert
        assertNotNull(result);
        assertEquals("Entradas", result.name());
    }

    @Test
    void getCategoryById_WithNonExistentCategory_ThrowsException() {
        // Arrange
        when(categoryRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> categoryService.getCategoryById("invalid-id"));
    }
}
