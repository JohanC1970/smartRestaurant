package com.smartRestaurant.auth.model.entity;

import java.time.LocalDateTime;

import com.smartRestaurant.auth.model.enums.AuditEventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un registro de auditoría de seguridad.
 * Almacena eventos importantes relacionados con la seguridad del sistema.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "security_audit_logs", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_event_type", columnList = "event_type"),
        @Index(name = "idx_timestamp", columnList = "timestamp"),
        @Index(name = "idx_success", columnList = "success")
})
public class SecurityAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID del usuario que generó el evento (puede ser null para eventos sin usuario
     * autenticado)
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * Email del usuario (almacenado para referencia incluso si el usuario es
     * eliminado)
     */
    @Column(length = 150)
    private String userEmail;

    /**
     * Tipo de evento de auditoría
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private AuditEventType eventType;

    /**
     * Timestamp del evento
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Dirección IP desde donde se originó el evento
     */
    @Column(name = "ip_address", length = 45) // IPv6 max length
    private String ipAddress;

    /**
     * User Agent del navegador/cliente
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Detalles adicionales del evento en formato JSON o texto
     */
    @Column(columnDefinition = "TEXT")
    private String details;

    /**
     * Indica si el evento fue exitoso o fallido
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean success = true;

    /**
     * Mensaje de error si el evento falló
     */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    /**
     * Verifica si el evento es crítico
     * 
     * @return true si el evento requiere atención
     */
    public boolean isCritical() {
        return eventType != null && eventType.isCritical();
    }

    /**
     * Verifica si el evento fue un fallo
     * 
     * @return true si el evento no fue exitoso
     */
    public boolean isFailed() {
        return !success;
    }
}
