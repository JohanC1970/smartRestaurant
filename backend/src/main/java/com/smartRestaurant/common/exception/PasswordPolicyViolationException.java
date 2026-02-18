package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando una contraseña no cumple con las políticas de
 * seguridad
 */
public class PasswordPolicyViolationException extends AuthException {

    public PasswordPolicyViolationException() {
        super("La contraseña no cumple con las políticas de seguridad", "PASSWORD_POLICY_VIOLATION");
    }

    public PasswordPolicyViolationException(String message) {
        super(message, "PASSWORD_POLICY_VIOLATION");
    }
}
