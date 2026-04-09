package com.smartRestaurant.auth.service;

import com.smartRestaurant.auth.dto.request.ChangeRoleRequest;
import com.smartRestaurant.auth.dto.request.UpdateUserRequest;
import com.smartRestaurant.auth.dto.response.UserResponse;
import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.AuditEventType;
import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;
import com.smartRestaurant.auth.repository.UserRepository;
import com.smartRestaurant.auth.service.impl.AdminServiceImpl;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@test.com")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .isEmailVerified(true)
                .build();
    }

    @Test
    void getAllUsers_WithNoFilters_ReturnsAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserResponse> result = adminService.getAllUsers(null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getFirstName());
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_WithRoleFilter_ReturnsFilteredUsers() {
        // Arrange
        when(userRepository.findByRole(UserRole.CUSTOMER)).thenReturn(Arrays.asList(testUser));

        // Act
        List<UserResponse> result = adminService.getAllUsers(UserRole.CUSTOMER, null);

        // Assert
        assertEquals(1, result.size());
        verify(userRepository).findByRole(UserRole.CUSTOMER);
    }

    @Test
    void getAllUsers_WithStatusFilter_ReturnsFilteredUsers() {
        // Arrange
        when(userRepository.findByStatus(UserStatus.ACTIVE)).thenReturn(Arrays.asList(testUser));

        // Act
        List<UserResponse> result = adminService.getAllUsers(null, UserStatus.ACTIVE);

        // Assert
        assertEquals(1, result.size());
        verify(userRepository).findByStatus(UserStatus.ACTIVE);
    }

    @Test
    void getAllUsers_WithBothFilters_ReturnsFilteredUsers() {
        // Arrange
        when(userRepository.findByRoleAndStatus(UserRole.CUSTOMER, UserStatus.ACTIVE))
                .thenReturn(Arrays.asList(testUser));

        // Act
        List<UserResponse> result = adminService.getAllUsers(UserRole.CUSTOMER, UserStatus.ACTIVE);

        // Assert
        assertEquals(1, result.size());
        verify(userRepository).findByRoleAndStatus(UserRole.CUSTOMER, UserStatus.ACTIVE);
    }

    @Test
    void getUserById_WithValidId_ReturnsUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserResponse result = adminService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Juan", result.getFirstName());
        assertEquals("juan@test.com", result.getEmail());
    }

    @Test
    void getUserById_WithInvalidId_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> adminService.getUserById(999L));
    }

    @Test
    void updateUser_WithValidData_UpdatesUser() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Carlos");
        request.setLastName("García");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse result = adminService.updateUser(1L, request);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.USER_UPDATED), anyString(), isNull(), isNull());
    }

    @Test
    void changeRole_WithDifferentRole_ChangesRole() {
        // Arrange
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setRole(UserRole.WAITER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse result = adminService.changeRole(1L, request);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.USER_UPDATED), anyString(), isNull(), isNull());
    }

    @Test
    void changeRole_WithSameRole_ThrowsException() {
        // Arrange
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setRole(UserRole.CUSTOMER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> adminService.changeRole(1L, request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deactivateUser_WithActiveUser_DeactivatesUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse result = adminService.deactivateUser(1L);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.USER_UPDATED), anyString(), isNull(), isNull());
    }

    @Test
    void deactivateUser_WithInactiveUser_ThrowsException() {
        // Arrange
        testUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> adminService.deactivateUser(1L));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deactivateUser_WithBannedUser_ThrowsException() {
        // Arrange
        testUser.setStatus(UserStatus.BANNED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> adminService.deactivateUser(1L));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void activateUser_WithInactiveUser_ActivatesUser() {
        // Arrange
        testUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponse result = adminService.activateUser(1L);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.USER_UPDATED), anyString(), isNull(), isNull());
    }

    @Test
    void activateUser_WithActiveUser_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> adminService.activateUser(1L));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void activateUser_WithBannedUser_ThrowsException() {
        // Arrange
        testUser.setStatus(UserStatus.BANNED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> adminService.activateUser(1L));
        verify(userRepository, never()).save(any(User.class));
    }
}
