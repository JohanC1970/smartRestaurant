package com.smartRestaurant.auth.service;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.OtpTokenType;

/**
 * Servicio para la gestión de códigos OTP (One-Time Password)
 */
public interface OtpService {

    /**
     * Genera un nuevo código OTP para el usuario y tipo especificado
     * Invalida cualquier token anterior del mismo tipo para este usuario
     * 
     * @param user Usuario para el cual se genera el OTP
     * @param type Tipo de OTP
     * @return El código OTP generado (6 dígitos)
     */
    String generateOtp(User user, OtpTokenType type);

    /**
     * Valida si un código OTP es correcto, no ha expirado y no ha sido usado
     * No marca el token como usado
     * 
     * @param user Usuario
     * @param code Código OTP a validar
     * @param type Tipo de OTP
     * @return true si el código es válido
     */
    boolean validateOtp(User user, String code, OtpTokenType type);

    /**
     * Valida y consume (marca como usado) un código OTP
     * 
     * @param user Usuario
     * @param code Código OTP a consumir
     * @param type Tipo de OTP
     * @return true si el código era válido y se consumió correctamente
     */
    boolean consumeOtp(User user, String code, OtpTokenType type);

    /**
     * Invalida todos los tokens de un tipo específico para un usuario
     * 
     * @param user Usuario
     * @param type Tipo de token
     */
    void invalidateUserTokens(User user, OtpTokenType type);
}
