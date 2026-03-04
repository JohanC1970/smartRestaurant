package com.smartRestaurant.auth.service;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.AuditEventType;

/**
 * Servicio para gestionar la auditoría de eventos de seguridad.
 */
public interface AuditService {

    /**
     * Registra un evento de auditoría exitoso
     * 
     * @param user      Usuario que generó el evento
     * @param eventType Tipo de evento
     * @param details   Detalles adicionales del evento
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User Agent del navegador
     */
    void logEvent(User user, AuditEventType eventType, String details, String ipAddress, String userAgent);

    /**
     * Registra un evento de auditoría con estado de éxito/fallo
     * 
     * @param user      Usuario que generó el evento (puede ser null)
     * @param eventType Tipo de evento
     * @param details   Detalles adicionales del evento
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User Agent del navegador
     * @param success   Indica si el evento fue exitoso
     */
    void logEvent(User user, AuditEventType eventType, String details, String ipAddress, String userAgent,
            boolean success);

    /**
     * Registra un evento de auditoría fallido con mensaje de error
     * 
     * @param user         Usuario que generó el evento (puede ser null)
     * @param eventType    Tipo de evento
     * @param details      Detalles adicionales del evento
     * @param ipAddress    Dirección IP del cliente
     * @param userAgent    User Agent del navegador
     * @param errorMessage Mensaje de error
     */
    void logFailedEvent(User user, AuditEventType eventType, String details, String ipAddress, String userAgent,
            String errorMessage);

    /**
     * Registra un evento de auditoría usando solo el email del usuario
     * Útil cuando el usuario no existe en la base de datos
     * 
     * @param email     Email del usuario
     * @param eventType Tipo de evento
     * @param details   Detalles adicionales del evento
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User Agent del navegador
     * @param success   Indica si el evento fue exitoso
     */
    void logEventByEmail(String email, AuditEventType eventType, String details, String ipAddress, String userAgent,
            boolean success);
}
