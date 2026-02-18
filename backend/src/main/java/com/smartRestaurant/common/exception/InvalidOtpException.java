package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando un código OTP es inválido o ha expirado
 */
public class InvalidOtpException extends AuthException {

    public InvalidOtpException() {
        super("Código OTP inválido o expirado", "INVALID_OTP");
    }

    public InvalidOtpException(String message) {
        super(message, "INVALID_OTP");
    }
}
