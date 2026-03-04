package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando se intenta reutilizar una contraseña anterior
 */
public class PasswordReuseException extends AuthException {

    public PasswordReuseException() {
        super("No puede reutilizar su contraseña anterior", "PASSWORD_REUSE");
    }

    public PasswordReuseException(String message) {
        super(message, "PASSWORD_REUSE");
    }
}
