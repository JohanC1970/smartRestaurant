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
     * Cambia la contraseña en el primer login (sin OTP ni contraseña actual)
     * Solo para usuarios con requiresPasswordChange = true
     * 
     * @param email       Email del usuario
     * @param newPassword Nueva contraseña
     * @return AuthResponse con nuevos tokens JWT para mantener la sesión
     */
    AuthResponse changePasswordFirstLogin(String email, String newPassword);

    /**
     * Cierra la sesión del usuario invalidando su refresh token (RF-12)
     * 
     * @param refreshToken Token de refresco a invalidar
     */
    void logout(String refreshToken);

    /**
     * Obtiene la información del usuario actualmente autenticado
     * 
     * @param email Email del usuario autenticado
     * @return UserResponse con la información del usuario
     */
    com.smartRestaurant.auth.dto.response.UserResponse getCurrentUser(String email);

    /**
     * Procesa login/registro con proveedor social (Google, Facebook, GitHub)
     * 
     * @param request Solicitud con proveedor y token de acceso
     * @return AuthResponse con tokens JWT
     */
    //AuthResponse socialLogin(com.smartRestaurant.auth.dto.request.SocialLoginRequest request);
}
