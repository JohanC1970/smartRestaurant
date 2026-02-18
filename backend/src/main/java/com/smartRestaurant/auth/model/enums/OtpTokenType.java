package com.smartRestaurant.auth.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum que define los tipos de tokens OTP en el sistema.
 * Cada tipo tiene un propósito específico y tiempo de expiración diferente.
 */
@Getter
@RequiredArgsConstructor
public enum OtpTokenType {

    // Verificación de email al registrarse
    VERIFICACION_EMAIL("Verificación de Email", 24 * 60), // 24 horas

    // Recuperación de contraseña olvidada
    RECUPERACION_PASSWORD("Recuperación de Contraseña", 30), // 30 minutos

    // Desbloqueo de cuenta después de múltiples intentos fallidos
    DESBLOQUEO_CUENTA("Desbloqueo de Cuenta", 60), // 1 hora

    // Autenticación de doble factor (2FA)
    LOGIN_2FA("Autenticación 2FA", 5); // 5 minutos

    // Nombre legible para mostrar en la interfaz de usuario
    private final String displayName;

    // Tiempo de expiración en minutos
    private final int expirationMinutes;

    /**
     * Verifica si este tipo de OTP es para verificación de email
     * 
     * @return true si es para verificación de email
     */
    public boolean isEmailVerification() {
        return this == VERIFICACION_EMAIL;
    }

    /**
     * Verifica si este tipo de OTP es para recuperación de contraseña
     * 
     * @return true si es para recuperación de contraseña
     */
    public boolean isPasswordRecovery() {
        return this == RECUPERACION_PASSWORD;
    }

    /**
     * Verifica si este tipo de OTP es para desbloqueo de cuenta
     * 
     * @return true si es para desbloqueo de cuenta
     */
    public boolean isAccountUnlock() {
        return this == DESBLOQUEO_CUENTA;
    }
}
