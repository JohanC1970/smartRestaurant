package com.smartRestaurant.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.mockito.ArgumentCaptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.smartRestaurant.auth.dto.request.LoginRequest;
import com.smartRestaurant.auth.dto.request.RegisterRequest;
import com.smartRestaurant.auth.dto.request.VerifyRequest;
import com.smartRestaurant.auth.dto.response.AuthResponse;
import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.OtpTokenType;
import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;
import com.smartRestaurant.auth.repository.UserRepository;
import com.smartRestaurant.auth.service.impl.AuthenticationServiceImpl;
import com.smartRestaurant.auth.service.impl.JwtServiceImpl;
import com.smartRestaurant.common.exception.AccountLockedException;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock(name = "jwtService")
    private JwtServiceImpl jwtService;
    @Mock
    private OtpService otpService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthenticationServiceImpl authService;

    @Test
    void registerPublic_ShouldSaveUserAndSendEmail_WhenEmailIsNew() {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("test@example.com")
                .password("Password123!")
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(otpService.generateOtp(any(User.class), eq(OtpTokenType.VERIFICACION_EMAIL))).thenReturn("123456");

        authService.registerPublic(request);

        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(eq("test@example.com"), eq("John"), eq("123456"));
    }

    @Test
    void login_ShouldReturn2faRequired_WhenCredentialsAreValid() {
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPass")
                .firstName("John")
                .status(UserStatus.ACTIVE)
                .build();

        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "encodedPass")).thenReturn(true);
        when(otpService.generateOtp(user, OtpTokenType.LOGIN_2FA)).thenReturn("123456");

        AuthResponse response = authService.login(request);

        assertTrue(response.is2faRequired());
        verify(emailService).sendVerificationEmail(eq("test@example.com"), eq("John"), eq("123456"));
    }

    @Test
    void login_ShouldLockAccount_After3FailedAttempts() {
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPass")
                .failedLoginAttempts(2)
                .status(UserStatus.ACTIVE)
                .build();

        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("WrongPass")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass", "encodedPass")).thenReturn(false);

        assertThrows(AccountLockedException.class, () -> authService.login(request));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(UserStatus.BANNED, userCaptor.getValue().getStatus());
        verify(otpService).generateOtp(userCaptor.getValue(), OtpTokenType.DESBLOQUEO_CUENTA);
    }

    @Test
    void verify2fa_ShouldReturnTokens_WhenOtpIsValid() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.CUSTOMER)
                .build();

        VerifyRequest request = VerifyRequest.builder()
                .email("test@example.com")
                .code("123456")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(otpService.validateOtp(user, "123456", OtpTokenType.LOGIN_2FA)).thenReturn(true);
        String accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";
        String refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicmVmcmVzaCI6dHJ1ZX0.5z3z5z5z5z5z5z5z5z5z5z5z5z5z5z5z5z5z5z5z5z5z";
        when(jwtService.generateAccessToken(user)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);

        AuthResponse response = authService.verify2fa(request);

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }
}
