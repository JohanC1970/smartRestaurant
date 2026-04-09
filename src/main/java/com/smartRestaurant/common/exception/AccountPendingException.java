package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando se intenta acceder con una cuenta pendiente de verificación
 */
public class AccountPendingException extends AuthException {
    
    public AccountPendingException(String message) {
        super(message, "ACCOUNT_PENDING");
    }
    
    public AccountPendingException() {
        super("Debe verificar su email antes de iniciar sesión", "ACCOUNT_PENDING");
    }
}
