package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando un usuario no es encontrado
 */
public class UserNotFoundException extends AuthException {

    public UserNotFoundException() {
        super("No se encontró el usuario solicitado", "USER_NOT_FOUND");
    }

    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND");
    }

}
