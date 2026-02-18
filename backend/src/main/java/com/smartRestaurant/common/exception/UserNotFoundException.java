package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando un usuario no es encontrado
 */
public class UserNotFoundException extends AuthException {

    public UserNotFoundException() {
        super("Usuario no encontrado", "USER_NOT_FOUND");
    }

    public UserNotFoundException(String message) {
        super(message, "USER_NOT_FOUND");
    }

}
