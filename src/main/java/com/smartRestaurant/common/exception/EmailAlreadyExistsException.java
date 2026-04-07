package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando un email ya está registrado
 */
public class EmailAlreadyExistsException extends AuthException {

    public EmailAlreadyExistsException() {
        super("Este correo electrónico ya está registrado.", "EMAIL_ALREADY_EXISTS");
    }

    /** Constructor con mensaje personalizado (sin exponer el email). */
    public EmailAlreadyExistsException(String customMessage) {
        super(customMessage, "EMAIL_ALREADY_EXISTS");
    }
}
