package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando una cuenta está bloqueada
 */
public class AccountLockedException extends AuthException {

    public AccountLockedException() {
        super("Su cuenta ha sido bloqueada. Por favor revise su correo electrónico para desbloquearla.", "ACCOUNT_LOCKED");
    }

    public AccountLockedException(String message) {
        super(message, "ACCOUNT_LOCKED");
    }
}
