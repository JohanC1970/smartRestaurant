package com.smartRestaurant.auth.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartRestaurant.auth.model.entity.OtpToken;
import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.OtpTokenType;
import com.smartRestaurant.auth.repository.OtpTokenRepository;
import com.smartRestaurant.auth.service.OtpService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String generateOtp(User user, OtpTokenType type) {
        // Invalidar tokens anteriores del mismo tipo para este usuario
        invalidateUserTokens(user, type);

        // Generar código de 6 dígitos
        int code = secureRandom.nextInt(1000000);
        String otpCode = String.format("%06d", code);

        // Crear y guardar el nuevo token
        // La fecha de expiración se calcula automáticamente en @PrePersist de OtpToken
        // basado en el tipo
        OtpToken token = OtpToken.builder()
                .usuario(user)
                .codigoOtp(otpCode)
                .tipo(type)
                .usado(false)
                .build();

        otpTokenRepository.save(token);

        return otpCode;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateOtp(User user, String code, OtpTokenType type) {
        return otpTokenRepository.findValidTokenByUser(user, code, type, LocalDateTime.now())
                .isPresent();
    }

    @Override
    @Transactional
    public boolean consumeOtp(User user, String code, OtpTokenType type) {
        return otpTokenRepository.findValidTokenByUser(user, code, type, LocalDateTime.now())
                .map(token -> {
                    token.markAsUsed();
                    otpTokenRepository.save(token);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public void invalidateUserTokens(User user, OtpTokenType type) {
        otpTokenRepository.invalidateUserTokensByType(user, type, LocalDateTime.now());
    }
}
