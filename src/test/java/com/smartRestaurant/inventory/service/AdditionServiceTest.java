package com.smartRestaurant.inventory.service;

import com.smartRestaurant.inventory.Repository.AdditionRepository;
import com.smartRestaurant.inventory.Service.impl.AdditionServiceImpl;
import com.smartRestaurant.inventory.dto.Addition.CreateAdditionDTO;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.mapper.AdditionMapper;
import com.smartRestaurant.inventory.mapper.ShowAdditionDetailMapper;
import com.smartRestaurant.inventory.model.Addition;
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
class AdditionServiceTest {

    @Mock
    private AdditionRepository additionRepository;

    @Mock
    private AdditionMapper additionMapper;

    @Mock
    private ShowAdditionDetailMapper showAdditionDetailMapper;

    @InjectMocks
    private AdditionServiceImpl additionService;

    private Addition testAddition;

    @BeforeEach
    void setUp() {
        testAddition = new Addition();
        testAddition.setId("addition-1");
        testAddition.setName("Queso Extra");
        testAddition.setPrice(2000.0);
        testAddition.setState(State.ACTIVE);
    }

    @Test
    void create_WithValidData_CreatesAddition() {
        // Arrange
        CreateAdditionDTO createDTO = new CreateAdditionDTO("Queso Extra", "Queso adicional", 2000.0);
        
        when(additionRepository.findByName("Queso Extra")).thenReturn(Optional.empty());
        when(additionMapper.toEntity(createDTO)).thenReturn(testAddition);
        when(additionRepository.save(any(Addition.class))).thenReturn(testAddition);

        // Act
        additionService.create(createDTO);

        // Assert
        verify(additionRepository).save(any(Addition.class));
    }

    @Test
    void create_WithExistingAddition_ThrowsException() {
        // Arrange
        CreateAdditionDTO createDTO = new CreateAdditionDTO("Queso Extra", "Queso adicional", 2000.0);
        
        when(additionRepository.findByName("Queso Extra")).thenReturn(Optional.of(testAddition));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> additionService.create(createDTO));
    }

    @Test
    void update_WithValidData_UpdatesAddition() {
        // Arrange
        when(additionRepository.findById("addition-1")).thenReturn(Optional.of(testAddition));
        when(additionRepository.save(any(Addition.class))).thenReturn(testAddition);

        // Act & Assert
        assertDoesNotThrow(() -> additionService.update("addition-1", null));
        verify(additionRepository).save(testAddition);
    }

    @Test
    void update_WithNonExistentAddition_ThrowsException() {
        // Arrange
        when(additionRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> additionService.update("invalid-id", null));
    }

    @Test
    void delete_WithValidId_SetsAdditionInactive() {
        // Arrange
        when(additionRepository.findById("addition-1")).thenReturn(Optional.of(testAddition));
        when(additionRepository.save(any(Addition.class))).thenReturn(testAddition);

        // Act
        additionService.delete("addition-1");

        // Assert
        verify(additionRepository).save(any(Addition.class));
    }

    @Test
    void delete_WithNonExistentAddition_ThrowsException() {
        // Arrange
        when(additionRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> additionService.delete("invalid-id"));
    }

    @Test
    void getById_WithValidId_ReturnsAddition() {
        // Arrange
        when(additionRepository.findById("addition-1")).thenReturn(Optional.of(testAddition));

        // Act & Assert
        assertDoesNotThrow(() -> additionService.getById("addition-1"));
    }

    @Test
    void getById_WithNonExistentAddition_ThrowsException() {
        // Arrange
        when(additionRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> additionService.getById("invalid-id"));
    }
}
