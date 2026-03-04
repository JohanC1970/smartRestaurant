package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando una cuenta está bloqueada
 */
public class AccountLockedException extends AuthException {

    public AccountLockedException() {
        super("La cuenta está bloqueada", "ACCOUNT_LOCKED");
    }

    public AccountLockedException(String message) {
        super(message, "ACCOUNT_LOCKED");
    }
}
