package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando un email ya está registrado
 */
public class EmailAlreadyExistsException extends AuthException {

    public EmailAlreadyExistsException() {
        super("El email ya está registrado", "EMAIL_ALREADY_EXISTS");
    }

    public EmailAlreadyExistsException(String email) {
        super("El email " + email + " ya está registrado", "EMAIL_ALREADY_EXISTS");
    }
}
