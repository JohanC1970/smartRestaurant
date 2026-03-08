package com.smartRestaurant.auth.service;

import com.smartRestaurant.auth.model.entity.SecurityAuditLog;
import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.AuditEventType;
import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;
import com.smartRestaurant.auth.repository.SecurityAuditLogRepository;
import com.smartRestaurant.auth.service.impl.AuditServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private SecurityAuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditServiceImpl auditService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void logEvent_WithUser_SavesAuditLog() {
        // Arrange
        ArgumentCaptor<SecurityAuditLog> captor = ArgumentCaptor.forClass(SecurityAuditLog.class);

        // Act
        auditService.logEvent(testUser, AuditEventType.LOGIN_SUCCESS, "Login exitoso", "192.168.1.1", "Mozilla");

        // Assert
        verify(auditLogRepository).save(captor.capture());
        SecurityAuditLog savedLog = captor.getValue();
        
        assertEquals(1L, savedLog.getUserId());
        assertEquals("test@example.com", savedLog.getUserEmail());
        assertEquals(AuditEventType.LOGIN_SUCCESS, savedLog.getEventType());
        assertEquals("Login exitoso", savedLog.getDetails());
        assertEquals("192.168.1.1", savedLog.getIpAddress());
        assertEquals("Mozilla", savedLog.getUserAgent());
        assertTrue(savedLog.isSuccess());
    }

    @Test
    void logEvent_WithNullUser_SavesAuditLogWithNullUserId() {
        // Arrange
        ArgumentCaptor<SecurityAuditLog> captor = ArgumentCaptor.forClass(SecurityAuditLog.class);

        // Act
        auditService.logEvent(null, AuditEventType.LOGIN_FAILED, "Usuario no encontrado", "192.168.1.1", "Mozilla");

        // Assert
        verify(auditLogRepository).save(captor.capture());
        SecurityAuditLog savedLog = captor.getValue();
        
        assertNull(savedLog.getUserId());
        assertNull(savedLog.getUserEmail());
        assertEquals(AuditEventType.LOGIN_FAILED, savedLog.getEventType());
    }

    @Test
    void logEvent_WithSuccessFlag_SavesWithCorrectStatus() {
        // Arrange
        ArgumentCaptor<SecurityAuditLog> captor = ArgumentCaptor.forClass(SecurityAuditLog.class);

        // Act
        auditService.logEvent(testUser, AuditEventType.LOGIN_FAILED, "Contraseña incorrecta", 
                "192.168.1.1", "Mozilla", false);

        // Assert
        verify(auditLogRepository).save(captor.capture());
        SecurityAuditLog savedLog = captor.getValue();
        
        assertFalse(savedLog.isSuccess());
    }

    @Test
    void logFailedEvent_WithErrorMessage_SavesFailedLog() {
        // Arrange
        ArgumentCaptor<SecurityAuditLog> captor = ArgumentCaptor.forClass(SecurityAuditLog.class);

        // Act
        auditService.logFailedEvent(testUser, AuditEventType.TWO_FA_FAILED, 
                "Código inválido", "192.168.1.1", "Mozilla", "OTP expirado");

        // Assert
        verify(auditLogRepository).save(captor.capture());
        SecurityAuditLog savedLog = captor.getValue();
        
        assertFalse(savedLog.isSuccess());
        assertEquals("OTP expirado", savedLog.getErrorMessage());
        assertEquals(AuditEventType.TWO_FA_FAILED, savedLog.getEventType());
    }

    @Test
    void logEventByEmail_WithEmail_SavesAuditLog() {
        // Arrange
        ArgumentCaptor<SecurityAuditLog> captor = ArgumentCaptor.forClass(SecurityAuditLog.class);

        // Act
        auditService.logEventByEmail("test@example.com", AuditEventType.PASSWORD_RESET_REQUESTED, 
                "Solicitud de reset", "192.168.1.1", "Mozilla", true);

        // Assert
        verify(auditLogRepository).save(captor.capture());
        SecurityAuditLog savedLog = captor.getValue();
        
        assertNull(savedLog.getUserId());
        assertEquals("test@example.com", savedLog.getUserEmail());
        assertTrue(savedLog.isSuccess());
    }

    @Test
    void logEvent_WhenRepositoryThrowsException_DoesNotPropagateException() {
        // Arrange
        when(auditLogRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        // Act & Assert - no exception should be thrown
        assertDoesNotThrow(() -> 
            auditService.logEvent(testUser, AuditEventType.LOGIN_SUCCESS, "Test", "192.168.1.1", "Mozilla")
        );
    }
}
