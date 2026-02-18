package com.smartRestaurant.auth.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartRestaurant.auth.dto.response.AuditLogResponse;
import com.smartRestaurant.auth.model.entity.SecurityAuditLog;
import com.smartRestaurant.auth.model.enums.AuditEventType;
import com.smartRestaurant.auth.repository.SecurityAuditLogRepository;

import lombok.RequiredArgsConstructor;

/**
 * Controlador para gestionar consultas de logs de auditoría.
 * Solo accesible para administradores.
 */
@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final SecurityAuditLogRepository auditLogRepository;

    /**
     * Obtiene todos los logs de auditoría con paginación
     */
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SecurityAuditLog> logs = auditLogRepository.findAll(pageable);

        Page<AuditLogResponse> response = logs.map(this::toResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene logs de un usuario específico
     */
    @GetMapping("/by-user")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByUser(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SecurityAuditLog> logs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);

        Page<AuditLogResponse> response = logs.map(this::toResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene logs por tipo de evento
     */
    @GetMapping("/by-event-type")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByEventType(
            @RequestParam AuditEventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SecurityAuditLog> logs = auditLogRepository.findByEventTypeOrderByTimestampDesc(eventType, pageable);

        Page<AuditLogResponse> response = logs.map(this::toResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene logs en un rango de fechas
     */
    @GetMapping("/by-date-range")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SecurityAuditLog> logs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
                startDate, endDate, pageable);

        Page<AuditLogResponse> response = logs.map(this::toResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene logs fallidos (eventos no exitosos)
     */
    @GetMapping("/failed")
    public ResponseEntity<Page<AuditLogResponse>> getFailedLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SecurityAuditLog> logs = auditLogRepository.findBySuccessFalseOrderByTimestampDesc(pageable);

        Page<AuditLogResponse> response = logs.map(this::toResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene logs críticos (eventos que requieren atención)
     */
    @GetMapping("/critical")
    public ResponseEntity<Page<AuditLogResponse>> getCriticalLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<AuditEventType> criticalEvents = List.of(
                AuditEventType.LOGIN_FAILED,
                AuditEventType.ACCOUNT_LOCKED,
                AuditEventType.TWO_FA_FAILED);

        Pageable pageable = PageRequest.of(page, size);
        Page<SecurityAuditLog> logs = auditLogRepository.findByEventTypeInOrderByTimestampDesc(
                criticalEvents, pageable);

        Page<AuditLogResponse> response = logs.map(this::toResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene los últimos 10 eventos de un usuario
     */
    @GetMapping("/recent-by-user")
    public ResponseEntity<List<AuditLogResponse>> getRecentLogsByUser(@RequestParam Long userId) {
        List<SecurityAuditLog> logs = auditLogRepository.findTop10ByUserIdOrderByTimestampDesc(userId);

        List<AuditLogResponse> response = logs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene logs por dirección IP
     */
    @GetMapping("/by-ip")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByIp(
            @RequestParam String ipAddress,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SecurityAuditLog> logs = auditLogRepository.findByIpAddressOrderByTimestampDesc(ipAddress, pageable);

        Page<AuditLogResponse> response = logs.map(this::toResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Convierte una entidad SecurityAuditLog a DTO AuditLogResponse
     */
    private AuditLogResponse toResponse(SecurityAuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .userEmail(log.getUserEmail())
                .eventType(log.getEventType())
                .eventTypeName(log.getEventType() != null ? log.getEventType().getDisplayName() : null)
                .timestamp(log.getTimestamp())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .details(log.getDetails())
                .success(log.isSuccess())
                .errorMessage(log.getErrorMessage())
                .critical(log.isCritical())
                .build();
    }
}
