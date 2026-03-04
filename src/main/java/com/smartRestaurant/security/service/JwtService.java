package com.smartRestaurant.security.service;

import com.smartRestaurant.auth.model.entity.User;
import io.jsonwebtoken.Claims;

/**
 * Servicio para gestionar tokens JWT (JSON Web Tokens)
 * Proporciona funcionalidades para generar, validar y extraer información de
 * tokens
 */
public interface JwtService {

    /**
     * Genera un token de acceso (Access Token) para el usuario
     * 
     * @param user Usuario para el cual se generará el token
     * @return Token JWT de acceso
     */
    String generateAccessToken(User user);

    /**
     * Genera un token de refresco (Refresh Token) para el usuario
     * 
     * @param user Usuario para el cual se generará el token
     * @return Token JWT de refresco
     */
    String generateRefreshToken(User user);

    /**
     * Valida si un token es válido (firma correcta y no expirado)
     * 
     * @param token Token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    boolean validateToken(String token);

    /**
     * Extrae el nombre de usuario (email) del token
     * 
     * @param token Token JWT
     * @return Email del usuario
     */
    String extractUsername(String token);

    /**
     * Extrae el ID del usuario del token
     * 
     * @param token Token JWT
     * @return ID del usuario
     */
    Long extractUserId(String token);

    /**
     * Extrae todos los claims (reclamaciones) del token
     * 
     * @param token Token JWT
     * @return Claims del token
     */
    Claims extractAllClaims(String token);

    /**
     * Verifica si el token ha expirado
     * 
     * @param token Token JWT
     * @return true si el token ha expirado, false en caso contrario
     */
    boolean isTokenExpired(String token);

    /**
     * Extrae el rol del usuario del token
     * 
     * @param token Token JWT
     * @return Rol del usuario
     */
    String extractUserRole(String token);
}
