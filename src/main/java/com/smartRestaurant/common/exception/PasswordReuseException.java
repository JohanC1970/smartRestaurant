package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando se intenta reutilizar una contraseña anterior
 */
public class PasswordReuseException extends AuthException {

    public PasswordReuseException() {
        super("No puede usar la misma contraseña anterior. Por favor elija una contraseña diferente.", "PASSWORD_REUSE");
    }

    public PasswordReuseException(String message) {
        super(message, "PASSWORD_REUSE");
    }
}
