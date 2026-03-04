package com.smartRestaurant.auth.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartRestaurant.auth.model.entity.SecurityAuditLog;
import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.AuditEventType;
import com.smartRestaurant.auth.repository.SecurityAuditLogRepository;
import com.smartRestaurant.auth.service.AuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de auditoría de seguridad.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final SecurityAuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void logEvent(User user, AuditEventType eventType, String details, String ipAddress, String userAgent) {
        logEvent(user, eventType, details, ipAddress, userAgent, true);
    }

    @Override
    @Transactional
    public void logEvent(User user, AuditEventType eventType, String details, String ipAddress, String userAgent,
            boolean success) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .userId(user != null ? user.getId() : null)
                    .userEmail(user != null ? user.getEmail() : null)
                    .eventType(eventType)
                    .details(details)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .success(success)
                    .build();

            auditLogRepository.save(auditLog);

            // Log crítico en consola para eventos importantes
            if (auditLog.isCritical()) {
                log.warn("CRITICAL SECURITY EVENT: {} - User: {} - IP: {} - Success: {}",
                        eventType.getDisplayName(),
                        user != null ? user.getEmail() : "N/A",
                        ipAddress,
                        success);
            }
        } catch (Exception e) {
            // No lanzar excepción para no interrumpir el flujo principal
            // Solo registrar en logs
            log.error("Error al registrar evento de auditoría: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void logFailedEvent(User user, AuditEventType eventType, String details, String ipAddress, String userAgent,
            String errorMessage) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .userId(user != null ? user.getId() : null)
                    .userEmail(user != null ? user.getEmail() : null)
                    .eventType(eventType)
                    .details(details)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .success(false)
                    .errorMessage(errorMessage)
                    .build();

            auditLogRepository.save(auditLog);

            // Log en consola para eventos fallidos
            log.warn("FAILED SECURITY EVENT: {} - User: {} - IP: {} - Error: {}",
                    eventType.getDisplayName(),
                    user != null ? user.getEmail() : "N/A",
                    ipAddress,
                    errorMessage);
        } catch (Exception e) {
            log.error("Error al registrar evento de auditoría fallido: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void logEventByEmail(String email, AuditEventType eventType, String details, String ipAddress,
            String userAgent, boolean success) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .userId(null)
                    .userEmail(email)
                    .eventType(eventType)
                    .details(details)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .success(success)
                    .build();

            auditLogRepository.save(auditLog);

            if (auditLog.isCritical()) {
                log.warn("CRITICAL SECURITY EVENT: {} - Email: {} - IP: {} - Success: {}",
                        eventType.getDisplayName(),
                        email,
                        ipAddress,
                        success);
            }
        } catch (Exception e) {
            log.error("Error al registrar evento de auditoría por email: {}", e.getMessage(), e);
        }
    }
}
