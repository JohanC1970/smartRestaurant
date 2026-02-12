package com.smartRestaurant.auth.service;

/**
 * Servicio para el envío de correos electrónicos transaccionales
 */
public interface EmailService {

    /**
     * Envía un correo con el código OTP para verificación de email
     * 
     * @param to   Dirección de correo destino
     * @param name Nombre del usuario
     * @param otp  Código OTP generado
     */
    void sendVerificationEmail(String to, String name, String otp);

    /**
     * Envía un correo con el código OTP para recuperación de contraseña
     * 
     * @param to   Dirección de correo destino
     * @param name Nombre del usuario
     * @param otp  Código OTP generado
     */
    void sendPasswordRecoveryEmail(String to, String name, String otp);

    /**
     * Envía un correo con el código OTP para desbloqueo de cuenta
     * 
     * @param to   Dirección de correo destino
     * @param name Nombre del usuario
     * @param otp  Código OTP generado
     */
    void sendAccountUnlockEmail(String to, String name, String otp);
}
