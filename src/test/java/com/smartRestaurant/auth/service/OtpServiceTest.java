package com.smartRestaurant.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartRestaurant.auth.model.entity.OtpToken;
import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.OtpTokenType;
import com.smartRestaurant.auth.repository.OtpTokenRepository;
import com.smartRestaurant.auth.service.impl.OtpServiceImpl;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @InjectMocks
    private OtpServiceImpl otpService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();
    }

    @Test
    void generateOtp_ShouldReturnSixDigitCode() {
        String code = otpService.generateOtp(user, OtpTokenType.LOGIN_2FA);

        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));

        verify(otpTokenRepository).invalidateUserTokensByType(eq(user), eq(OtpTokenType.LOGIN_2FA),
                any(LocalDateTime.class));
        verify(otpTokenRepository).save(any(OtpToken.class));
    }

    @Test
    void validateOtp_ShouldReturnTrue_WhenTokenIsValid() {
        String code = "123456";
        OtpToken token = OtpToken.builder()
                .usuario(user)
                .codigoOtp(code)
                .tipo(OtpTokenType.LOGIN_2FA)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(5))
                .usado(false)
                .build();

        when(otpTokenRepository.findValidTokenByUser(eq(user), eq(code), eq(OtpTokenType.LOGIN_2FA),
                any(LocalDateTime.class)))
                .thenReturn(Optional.of(token));

        boolean isValid = otpService.validateOtp(user, code, OtpTokenType.LOGIN_2FA);

        assertTrue(isValid);
    }

    @Test
    void validateOtp_ShouldReturnFalse_WhenTokenIsNotFound() {
        String code = "123456";
        when(otpTokenRepository.findValidTokenByUser(eq(user), eq(code), eq(OtpTokenType.LOGIN_2FA),
                any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        boolean isValid = otpService.validateOtp(user, code, OtpTokenType.LOGIN_2FA);

        assertFalse(isValid);
    }

    @Test
    void consumeOtp_ShouldReturnTrueAndMarkAsUsed_WhenTokenIsValid() {
        String code = "123456";
        OtpToken token = OtpToken.builder()
                .usuario(user)
                .codigoOtp(code)
                .tipo(OtpTokenType.LOGIN_2FA)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(5))
                .usado(false)
                .build();

        when(otpTokenRepository.findValidTokenByUser(eq(user), eq(code), eq(OtpTokenType.LOGIN_2FA),
                any(LocalDateTime.class)))
                .thenReturn(Optional.of(token));

        boolean result = otpService.consumeOtp(user, code, OtpTokenType.LOGIN_2FA);

        assertTrue(result);
        assertTrue(token.isUsado());
        verify(otpTokenRepository).save(token);
    }

    @Test
    void consumeOtp_ShouldReturnFalse_WhenTokenIsNotFound() {
        String code = "123456";
        when(otpTokenRepository.findValidTokenByUser(eq(user), eq(code), eq(OtpTokenType.LOGIN_2FA),
                any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        boolean result = otpService.consumeOtp(user, code, OtpTokenType.LOGIN_2FA);

        assertFalse(result);
    }
}
