package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando se intenta acceder con una cuenta inactiva
 */
public class AccountInactiveException extends AuthException {
    
    public AccountInactiveException(String message) {
        super(message, "ACCOUNT_INACTIVE");
    }
    
    public AccountInactiveException() {
        super("Su cuenta ha sido desactivada. Contacte al administrador.", "ACCOUNT_INACTIVE");
    }
}
