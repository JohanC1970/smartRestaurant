package com.smartRestaurant.auth.dto.response;

import java.time.LocalDateTime;

import com.smartRestaurant.auth.model.enums.AuditEventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de logs de auditoría
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private AuditEventType eventType;
    private String eventTypeName;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;
    private String details;
    private boolean success;
    private String errorMessage;
    private boolean critical;
}
