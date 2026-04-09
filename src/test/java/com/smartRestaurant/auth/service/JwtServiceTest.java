package com.smartRestaurant.auth.service;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        // Set values via reflection as they are @Value annotated
        // Using a 256-bit (32 byte) key for HS256
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L); // 7 days
    }

    @Test
    void testGenerateAndValidateAccessToken() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.ADMIN)
                .build();

        // Act
        String token = jwtService.generateAccessToken(user);

        // Assert
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals("test@example.com", jwtService.extractUsername(token));
        assertEquals(1L, jwtService.extractUserId(token));
        assertEquals("ADMIN", jwtService.extractUserRole(token));
    }

    @Test
    void testGenerateAndValidateRefreshToken() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.ADMIN)
                .build();

        // Act
        String token = jwtService.generateRefreshToken(user);

        // Assert
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals("test@example.com", jwtService.extractUsername(token));
    }

    @Test
    void testExpiredToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L); // Set negative expiration
        User user = User.builder()
                .id(1L)
                .email("expired@example.com")
                .role(UserRole.CUSTOMER)
                .build();

        // Act
        String token = jwtService.generateAccessToken(user);

        // Assert
        assertFalse(jwtService.validateToken(token));
        assertThrows(Exception.class, () -> jwtService.extractUsername(token));
    }
}
