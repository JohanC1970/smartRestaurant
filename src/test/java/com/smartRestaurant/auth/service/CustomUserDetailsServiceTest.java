package com.smartRestaurant.auth.service;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;
import com.smartRestaurant.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .isEmailVerified(true)
                .build();
    }

    @Test
    void loadUserByUsername_WithValidEmail_ReturnsUserDetails() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CUSTOMER")));
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsername_WithNonExistentEmail_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, 
                () -> customUserDetailsService.loadUserByUsername("nonexistent@example.com"));
    }

    @Test
    void loadUserByUsername_WithLockedUser_ReturnsLockedUserDetails() {
        // Arrange
        testUser.lock("Test lock");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertFalse(userDetails.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_WithInactiveUser_ReturnsDisabledUserDetails() {
        // Arrange
        testUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertFalse(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsername_WithPendingUser_ReturnsEnabledUserDetails() {
        // Arrange
        testUser.setStatus(UserStatus.PENDING);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertTrue(userDetails.isEnabled());
    }
}
