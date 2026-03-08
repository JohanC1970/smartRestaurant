package com.smartRestaurant.auth.service;

import com.smartRestaurant.auth.dto.request.LoginRequest;
import com.smartRestaurant.auth.dto.request.RegisterAdminRequest;
import com.smartRestaurant.auth.dto.request.RegisterRequest;
import com.smartRestaurant.auth.dto.request.VerifyRequest;
import com.smartRestaurant.auth.dto.response.AuthResponse;
import com.smartRestaurant.auth.model.entity.RefreshToken;
import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.AuditEventType;
import com.smartRestaurant.auth.model.enums.OtpTokenType;
import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;
import com.smartRestaurant.auth.repository.RefreshTokenRepository;
import com.smartRestaurant.auth.repository.UserRepository;
import com.smartRestaurant.auth.service.impl.AuthenticationServiceImpl;
import com.smartRestaurant.auth.util.PasswordGenerator;
import com.smartRestaurant.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@test.com")
                .password("encodedPassword")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .isEmailVerified(true)
                .failedLoginAttempts(0)
                .build();
    }

    @Test
    void registerPublic_WithValidData_CreatesUser() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@test.com")
                .password("Password123!")
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(otpService.generateOtp(any(User.class), eq(OtpTokenType.VERIFICACION_EMAIL))).thenReturn("123456");

        // Act
        authenticationService.registerPublic(request);

        // Assert
        verify(userRepository).save(any(User.class));
        verify(otpService).generateOtp(any(User.class), eq(OtpTokenType.VERIFICACION_EMAIL));
        verify(emailService).sendVerificationEmail(eq("juan@test.com"), eq("Juan"), eq("123456"));
        verify(auditService).logEvent(any(User.class), eq(AuditEventType.USER_REGISTERED), anyString(), isNull(), isNull());
    }

    @Test
    void registerPublic_WithExistingEmail_ThrowsException() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@test.com")
                .build();

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authenticationService.registerPublic(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerEmployee_WithValidData_CreatesEmployeeWithTempPassword() {
        // Arrange
        RegisterAdminRequest request = new RegisterAdminRequest();
        request.setFirstName("María");
        request.setLastName("López");
        request.setEmail("maria@test.com");
        request.setRole(UserRole.WAITER);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedTempPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(otpService.generateOtp(any(User.class), eq(OtpTokenType.VERIFICACION_EMAIL))).thenReturn("123456");

        // Act
        try (MockedStatic<PasswordGenerator> mockedGenerator = mockStatic(PasswordGenerator.class)) {
            mockedGenerator.when(PasswordGenerator::generate).thenReturn("TempPass123!");
            authenticationService.registerEmployee(request);
        }

        // Assert
        verify(userRepository).save(any(User.class));
        verify(emailService).sendEmployeeCredentials(eq("maria@test.com"), eq("María"), anyString(), eq("123456"));
        verify(auditService).logEvent(any(User.class), eq(AuditEventType.EMPLOYEE_REGISTERED), anyString(), isNull(), isNull());
    }

    @Test
    void login_WithValidCredentials_SendsOtpAndReturns2faRequired() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("juan@test.com");
        request.setPassword("Password123!");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);
        when(otpService.generateOtp(any(User.class), eq(OtpTokenType.LOGIN_2FA))).thenReturn("123456");

        // Act
        AuthResponse response = authenticationService.login(request);

        // Assert
        assertTrue(response.is2faRequired());
        assertEquals("Código 2FA enviado a su correo", response.getMessage());
        verify(emailService).sendVerificationEmail(eq("juan@test.com"), eq("Juan"), eq("123456"));
        verify(userRepository).save(any(User.class)); // Reset failed attempts
    }

    @Test
    void login_WithInvalidPassword_IncrementsFailedAttempts() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("juan@test.com");
        request.setPassword("WrongPassword");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authenticationService.login(request));
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.LOGIN_FAILED), anyString(), isNull(), isNull(), eq(false));
    }

    @Test
    void login_WithThreeFailedAttempts_LocksAccount() {
        // Arrange
        testUser.setFailedLoginAttempts(2); // Ya tiene 2 intentos
        LoginRequest request = new LoginRequest();
        request.setEmail("juan@test.com");
        request.setPassword("WrongPassword");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", "encodedPassword")).thenReturn(false);
        when(otpService.generateOtp(any(User.class), eq(OtpTokenType.DESBLOQUEO_CUENTA))).thenReturn("123456");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authenticationService.login(request));
        verify(emailService).sendAccountUnlockEmail(eq("juan@test.com"), eq("Juan"), eq("123456"));
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.ACCOUNT_LOCKED), anyString(), isNull(), isNull());
    }

    @Test
    void login_WithLockedAccount_ThrowsException() {
        // Arrange
        testUser.lock("Demasiados intentos");
        LoginRequest request = new LoginRequest();
        request.setEmail("juan@test.com");
        request.setPassword("Password123!");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authenticationService.login(request));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_WithNonExistentUser_ThrowsException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@test.com");
        request.setPassword("Password123!");

        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authenticationService.login(request));
        verify(auditService).logEventByEmail(eq("nonexistent@test.com"), eq(AuditEventType.LOGIN_FAILED), 
                anyString(), isNull(), isNull(), eq(false));
    }

    @Test
    void verify2fa_WithValidOtp_ReturnsTokens() {
        // Arrange
        VerifyRequest request = new VerifyRequest();
        request.setEmail("juan@test.com");
        request.setCode("123456");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));
        when(otpService.validateOtp(testUser, "123456", OtpTokenType.LOGIN_2FA)).thenReturn(true);
        when(jwtService.generateAccessToken(testUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token");

        // Act
        AuthResponse response = authenticationService.verify2fa(request);

        // Assert
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertFalse(response.is2faRequired());
        verify(otpService).consumeOtp(testUser, "123456", OtpTokenType.LOGIN_2FA);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.TWO_FA_SUCCESS), anyString(), isNull(), isNull());
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.LOGIN_SUCCESS), anyString(), isNull(), isNull());
    }

    @Test
    void verify2fa_WithInvalidOtp_ThrowsException() {
        // Arrange
        VerifyRequest request = new VerifyRequest();
        request.setEmail("juan@test.com");
        request.setCode("wrong-code");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));
        when(otpService.validateOtp(testUser, "wrong-code", OtpTokenType.LOGIN_2FA)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authenticationService.verify2fa(request));
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.TWO_FA_FAILED), anyString(), isNull(), isNull(), eq(false));
        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void verifyEmail_WithValidOtp_ActivatesUser() {
        // Arrange
        testUser.setEmailVerified(false);
        testUser.setStatus(UserStatus.PENDING);
        
        VerifyRequest request = new VerifyRequest();
        request.setEmail("juan@test.com");
        request.setCode("123456");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));
        when(otpService.validateOtp(testUser, "123456", OtpTokenType.VERIFICACION_EMAIL)).thenReturn(true);

        // Act
        authenticationService.verifyEmail(request);

        // Assert
        verify(otpService).consumeOtp(testUser, "123456", OtpTokenType.VERIFICACION_EMAIL);
        verify(userRepository).save(any(User.class));
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.EMAIL_VERIFIED), anyString(), isNull(), isNull());
    }

    @Test
    void verifyEmail_WhenAlreadyVerified_ThrowsException() {
        // Arrange
        testUser.setEmailVerified(true);
        
        VerifyRequest request = new VerifyRequest();
        request.setEmail("juan@test.com");
        request.setCode("123456");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authenticationService.verifyEmail(request));
        verify(otpService, never()).validateOtp(any(), anyString(), any());
    }

    @Test
    void unlockAccount_WithValidOtp_UnlocksAccount() {
        // Arrange
        testUser.lock("Test lock");
        
        VerifyRequest request = new VerifyRequest();
        request.setEmail("juan@test.com");
        request.setCode("123456");

        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));
        when(otpService.validateOtp(testUser, "123456", OtpTokenType.DESBLOQUEO_CUENTA)).thenReturn(true);

        // Act
        authenticationService.unlockAccount(request);

        // Assert
        verify(otpService).consumeOtp(testUser, "123456", OtpTokenType.DESBLOQUEO_CUENTA);
        verify(userRepository).save(any(User.class));
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.ACCOUNT_UNLOCKED), anyString(), isNull(), isNull());
    }

    @Test
    void forgotPassword_WithValidEmail_SendsRecoveryEmail() {
        // Arrange
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));
        when(otpService.generateOtp(testUser, OtpTokenType.RECUPERACION_PASSWORD)).thenReturn("123456");

        // Act
        authenticationService.forgotPassword("juan@test.com");

        // Assert
        verify(emailService).sendPasswordRecoveryEmail(eq("juan@test.com"), eq("Juan"), eq("123456"));
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.PASSWORD_RESET_REQUESTED), anyString(), isNull(), isNull());
    }

    @Test
    void resetPassword_WithValidOtp_ResetsPassword() {
        // Arrange
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));
        when(otpService.validateOtp(testUser, "123456", OtpTokenType.RECUPERACION_PASSWORD)).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("newEncodedPassword");

        // Act
        authenticationService.resetPassword("juan@test.com", "123456", "NewPassword123!");

        // Assert
        verify(otpService).consumeOtp(testUser, "123456", OtpTokenType.RECUPERACION_PASSWORD);
        verify(userRepository).save(any(User.class));
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.PASSWORD_RESET_COMPLETED), anyString(), isNull(), isNull());
    }

    @Test
    void changePassword_WithValidCurrentPassword_ChangesPassword() {
        // Arrange
        testUser.setPreviousPasswordHash("oldEncodedPassword");
        
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("CurrentPass123!", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPass123!", "oldEncodedPassword")).thenReturn(false);
        when(otpService.validateOtp(testUser, "123456", OtpTokenType.RECUPERACION_PASSWORD)).thenReturn(true);
        when(passwordEncoder.encode("NewPass123!")).thenReturn("newEncodedPassword");

        // Act
        authenticationService.changePassword("juan@test.com", "CurrentPass123!", "NewPass123!", "123456");

        // Assert
        verify(otpService).consumeOtp(testUser, "123456", OtpTokenType.RECUPERACION_PASSWORD);
        verify(userRepository).save(any(User.class));
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.PASSWORD_CHANGED), anyString(), isNull(), isNull());
    }

    @Test
    void changePassword_WithIncorrectCurrentPassword_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPass", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            authenticationService.changePassword("juan@test.com", "WrongPass", "NewPass123!", "123456"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_WithReusedPreviousPassword_ThrowsException() {
        // Arrange
        testUser.setPreviousPasswordHash("previousEncodedPassword");
        
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("CurrentPass123!", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("OldPass123!", "previousEncodedPassword")).thenReturn(true);
        when(otpService.validateOtp(testUser, "123456", OtpTokenType.RECUPERACION_PASSWORD)).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            authenticationService.changePassword("juan@test.com", "CurrentPass123!", "OldPass123!", "123456"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void refreshToken_WithValidToken_ReturnsNewAccessToken() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        
        when(jwtService.extractUsername(refreshToken)).thenReturn("juan@test.com");
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));
        when(jwtService.validateToken(refreshToken)).thenReturn(true);
        when(jwtService.generateAccessToken(testUser)).thenReturn("new-access-token");

        // Act
        AuthResponse response = authenticationService.refreshToken(refreshToken);

        // Assert
        assertNotNull(response.getAccessToken());
        assertEquals("new-access-token", response.getAccessToken());
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.TOKEN_REFRESHED), anyString(), isNull(), isNull());
    }

    @Test
    void refreshToken_WithInvalidToken_ThrowsException() {
        // Arrange
        String refreshToken = "invalid-token";
        
        when(jwtService.extractUsername(refreshToken)).thenReturn("juan@test.com");
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));
        when(jwtService.validateToken(refreshToken)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authenticationService.refreshToken(refreshToken));
    }

    @Test
    void logout_WithValidToken_InvalidatesRefreshToken() {
        // Arrange
        String refreshToken = "refresh-token";
        
        when(jwtService.extractUsername(refreshToken)).thenReturn("juan@test.com");
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(testUser));

        // Act
        authenticationService.logout(refreshToken);

        // Assert
        verify(refreshTokenRepository).deleteByToken(refreshToken);
        verify(auditService).logEvent(eq(testUser), eq(AuditEventType.LOGOUT), anyString(), isNull(), isNull());
    }
}
