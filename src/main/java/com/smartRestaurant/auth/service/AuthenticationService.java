package com.smartRestaurant.auth.service;

import com.smartRestaurant.auth.dto.request.LoginRequest;
import com.smartRestaurant.auth.dto.request.RegisterAdminRequest;
import com.smartRestaurant.auth.dto.request.RegisterRequest;
import com.smartRestaurant.auth.dto.request.VerifyRequest;
import com.smartRestaurant.auth.dto.response.AuthResponse;

public interface AuthenticationService {

    void registerPublic(RegisterRequest request);

    void registerEmployee(RegisterAdminRequest request);

    void resendVerification(String email);

    AuthResponse login(LoginRequest request);

    AuthResponse verify2fa(VerifyRequest request);

    void verifyEmail(VerifyRequest request);

    void forgotPassword(String email);

    void resetPassword(String email, String otp, String newPassword);

    void unlockAccount(VerifyRequest request);

    AuthResponse refreshToken(String refreshToken);

    /**
     * Solicita un cambio de contraseña voluntario generando un OTP (RF-07)
     * 
     * @param email Email del usuario
     */
    void requestPasswordChange(String email);

    /**
     * Cambia la contraseña del usuario después de validar OTP (RF-07)
     * 
     * @param email           Email del usuario
     * @param currentPassword Contraseña actual
     * @param newPassword     Nueva contraseña
     * @param otp             Código OTP de verificación
     */
    void changePassword(String email, String currentPassword, String newPassword, String otp);

    /**
     * Cierra la sesión del usuario invalidando su refresh token (RF-12)
     * 
     * @param refreshToken Token de refresco a invalidar
     */
    void logout(String refreshToken);
}
