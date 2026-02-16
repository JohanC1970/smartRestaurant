package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando las credenciales son inválidas
 */
public class InvalidCredentialsException extends AuthException {

    public InvalidCredentialsException() {
        super("Credenciales inválidas", "INVALID_CREDENTIALS");
    }

    public InvalidCredentialsException(String message) {
        super(message, "INVALID_CREDENTIALS");
    }
}
