package com.smartRestaurant.auth.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum que define los tipos de eventos de auditoría de seguridad.
 * Cada evento representa una acción importante que debe ser registrada.
 */
@Getter
@RequiredArgsConstructor
public enum AuditEventType {

    // Eventos de autenticación
    LOGIN_SUCCESS("Inicio de sesión exitoso", "Usuario inició sesión correctamente"),
    LOGIN_FAILED("Inicio de sesión fallido", "Intento de inicio de sesión con credenciales inválidas"),
    LOGOUT("Cierre de sesión", "Usuario cerró sesión"),

    // Eventos de registro
    USER_REGISTERED("Usuario registrado", "Nuevo usuario registrado en el sistema"),
    EMPLOYEE_REGISTERED("Empleado registrado", "Empleado registrado por administrador"),

    // Eventos de verificación
    EMAIL_VERIFIED("Email verificado", "Usuario verificó su correo electrónico"),
    TWO_FA_SUCCESS("2FA exitoso", "Verificación 2FA completada exitosamente"),
    TWO_FA_FAILED("2FA fallido", "Verificación 2FA fallida"),

    // Eventos de contraseña
    PASSWORD_CHANGED("Contraseña cambiada", "Usuario cambió su contraseña"),
    PASSWORD_RESET_REQUESTED("Recuperación solicitada", "Usuario solicitó recuperación de contraseña"),
    PASSWORD_RESET_COMPLETED("Recuperación completada", "Usuario completó recuperación de contraseña"),

    // Eventos de cuenta
    ACCOUNT_LOCKED("Cuenta bloqueada", "Cuenta bloqueada por intentos fallidos"),
    ACCOUNT_UNLOCKED("Cuenta desbloqueada", "Cuenta desbloqueada exitosamente"),

    // Eventos de tokens
    TOKEN_REFRESHED("Token renovado", "Token de acceso renovado"),
    TOKEN_INVALIDATED("Token invalidado", "Token invalidado manualmente");

    // Nombre legible para mostrar en la interfaz de usuario
    private final String displayName;

    // Descripción detallada del evento
    private final String description;

    /**
     * Verifica si el evento es de tipo autenticación
     * 
     * @return true si es evento de autenticación
     */
    public boolean isAuthenticationEvent() {
        return this == LOGIN_SUCCESS || this == LOGIN_FAILED ||
                this == TWO_FA_SUCCESS || this == TWO_FA_FAILED;
    }

    /**
     * Verifica si el evento es de tipo registro
     * 
     * @return true si es evento de registro
     */
    public boolean isRegistrationEvent() {
        return this == USER_REGISTERED || this == EMPLOYEE_REGISTERED;
    }

    /**
     * Verifica si el evento es de tipo contraseña
     * 
     * @return true si es evento relacionado con contraseña
     */
    public boolean isPasswordEvent() {
        return this == PASSWORD_CHANGED || this == PASSWORD_RESET_REQUESTED ||
                this == PASSWORD_RESET_COMPLETED;
    }

    /**
     * Verifica si el evento es crítico (requiere atención)
     * 
     * @return true si el evento es crítico
     */
    public boolean isCritical() {
        return this == LOGIN_FAILED || this == ACCOUNT_LOCKED ||
                this == TWO_FA_FAILED;
    }
}
