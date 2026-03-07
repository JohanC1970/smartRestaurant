package com.smartRestaurant.inventory.service;

import com.smartRestaurant.inventory.Repository.SuplierRepository;
import com.smartRestaurant.inventory.Service.impl.SuplierServiceImpl;
import com.smartRestaurant.inventory.dto.Suplier.CreateSuplierDTO;
import com.smartRestaurant.inventory.dto.Suplier.GetSuplierDTO;
import com.smartRestaurant.inventory.dto.Suplier.UpdateSuplierDTO;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.mapper.SuplierMapper;
import com.smartRestaurant.inventory.model.State;
import com.smartRestaurant.inventory.model.Suplier;
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
class SuplierServiceTest {

    @Mock
    private SuplierRepository suplierRepository;

    @Mock
    private SuplierMapper suplierMapper;

    @InjectMocks
    private SuplierServiceImpl suplierService;

    private Suplier testSuplier;

    @BeforeEach
    void setUp() {
        testSuplier = new Suplier();
        testSuplier.setId("suplier-1");
        testSuplier.setName("Proveedor Test");
        testSuplier.setEmail("proveedor@test.com");
        testSuplier.setPhone("1234567890");
        testSuplier.setAddress("Calle Test 123");
        testSuplier.setState(State.ACTIVE);
    }

    @Test
    void getAll_ReturnsActiveSupliers() {
        // Arrange
        GetSuplierDTO suplierDTO = new GetSuplierDTO("suplier-1", "Proveedor Test", "proveedor@test.com", "1234567890", "Calle Test 123");
        
        when(suplierRepository.findAll()).thenReturn(Arrays.asList(testSuplier));
        when(suplierMapper.toDto(testSuplier)).thenReturn(suplierDTO);

        // Act
        List<GetSuplierDTO> result = suplierService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Proveedor Test", result.get(0).name());
    }

    @Test
    void create_WithValidData_CreatesSuplier() {
        // Arrange
        CreateSuplierDTO createDTO = new CreateSuplierDTO("Nuevo Proveedor", "nuevo@test.com", "9876543210", "Calle Nueva 456");
        
        when(suplierRepository.findByEmail("nuevo@test.com")).thenReturn(Optional.empty());
        when(suplierMapper.toEntity(createDTO)).thenReturn(testSuplier);
        when(suplierRepository.save(any(Suplier.class))).thenReturn(testSuplier);

        // Act
        suplierService.create(createDTO);

        // Assert
        verify(suplierRepository).save(any(Suplier.class));
    }

    @Test
    void create_WithExistingEmail_ThrowsException() {
        // Arrange
        CreateSuplierDTO createDTO = new CreateSuplierDTO("Proveedor Test", "proveedor@test.com", "1234567890", "Calle Test 123");
        
        when(suplierRepository.findByEmail("proveedor@test.com")).thenReturn(Optional.of(testSuplier));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> suplierService.create(createDTO));
        verify(suplierRepository, never()).save(any(Suplier.class));
    }

    @Test
    void update_WithValidData_UpdatesSuplier() {
        // Arrange
        UpdateSuplierDTO updateDTO = new UpdateSuplierDTO("Proveedor Actualizado", "actualizado@test.com", "1111111111", "Calle Actualizada 789");
        
        when(suplierRepository.findById("suplier-1")).thenReturn(Optional.of(testSuplier));
        when(suplierRepository.save(any(Suplier.class))).thenReturn(testSuplier);

        // Act
        suplierService.update("suplier-1", updateDTO);

        // Assert
        verify(suplierMapper).updateDto(updateDTO, testSuplier);
        verify(suplierRepository).save(testSuplier);
    }

    @Test
    void delete_WithValidId_SetsSuplierInactive() {
        // Arrange
        when(suplierRepository.findById("suplier-1")).thenReturn(Optional.of(testSuplier));
        when(suplierRepository.save(any(Suplier.class))).thenReturn(testSuplier);

        // Act
        suplierService.delete("suplier-1");

        // Assert
        verify(suplierRepository).save(any(Suplier.class));
    }

    @Test
    void getById_WithValidId_ReturnsSuplier() {
        // Arrange
        GetSuplierDTO suplierDTO = new GetSuplierDTO("suplier-1", "Proveedor Test", "proveedor@test.com", "1234567890", "Calle Test 123");
        
        when(suplierRepository.findById("suplier-1")).thenReturn(Optional.of(testSuplier));
        when(suplierMapper.toDto(testSuplier)).thenReturn(suplierDTO);

        // Act
        GetSuplierDTO result = suplierService.getById("suplier-1");

        // Assert
        assertNotNull(result);
        assertEquals("Proveedor Test", result.name());
    }

    @Test
    void getById_WithNonExistentSuplier_ThrowsException() {
        // Arrange
        when(suplierRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> suplierService.getById("invalid-id"));
    }
}
