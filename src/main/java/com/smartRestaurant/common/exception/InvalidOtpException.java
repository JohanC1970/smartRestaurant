package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando un código OTP es inválido o ha expirado
 */
public class InvalidOtpException extends AuthException {

    public InvalidOtpException() {
        super("El código de verificación es inválido o ha expirado. Solicite uno nuevo.", "INVALID_OTP");
    }

    public InvalidOtpException(String message) {
        super(message, "INVALID_OTP");
    }
}
