package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando las credenciales son inválidas
 */
public class InvalidCredentialsException extends AuthException {

    public InvalidCredentialsException() {
        super("El email o la contraseña son incorrectos", "INVALID_CREDENTIALS");
    }

    public InvalidCredentialsException(String message) {
        super(message, "INVALID_CREDENTIALS");
    }
}
