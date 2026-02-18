package com.smartRestaurant.auth.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartRestaurant.auth.model.entity.SecurityAuditLog;
import com.smartRestaurant.auth.model.enums.AuditEventType;

/**
 * Repositorio para gestionar logs de auditoría de seguridad.
 */
@Repository
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {

    /**
     * Encuentra todos los logs de un usuario específico
     * 
     * @param userId   ID del usuario
     * @param pageable Configuración de paginación
     * @return Página de logs del usuario
     */
    Page<SecurityAuditLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    /**
     * Encuentra logs por tipo de evento
     * 
     * @param eventType Tipo de evento
     * @param pageable  Configuración de paginación
     * @return Página de logs del tipo especificado
     */
    Page<SecurityAuditLog> findByEventTypeOrderByTimestampDesc(AuditEventType eventType, Pageable pageable);

    /**
     * Encuentra logs en un rango de fechas
     * 
     * @param startDate Fecha de inicio
     * @param endDate   Fecha de fin
     * @param pageable  Configuración de paginación
     * @return Página de logs en el rango especificado
     */
    Page<SecurityAuditLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    /**
     * Encuentra logs fallidos (eventos no exitosos)
     * 
     * @param pageable Configuración de paginación
     * @return Página de logs fallidos
     */
    Page<SecurityAuditLog> findBySuccessFalseOrderByTimestampDesc(Pageable pageable);

    /**
     * Encuentra logs críticos (eventos que requieren atención)
     * 
     * @param eventTypes Lista de tipos de eventos críticos
     * @param pageable   Configuración de paginación
     * @return Página de logs críticos
     */
    Page<SecurityAuditLog> findByEventTypeInOrderByTimestampDesc(
            List<AuditEventType> eventTypes,
            Pageable pageable);

    /**
     * Encuentra logs de un usuario en un rango de fechas
     * 
     * @param userId    ID del usuario
     * @param startDate Fecha de inicio
     * @param endDate   Fecha de fin
     * @param pageable  Configuración de paginación
     * @return Página de logs del usuario en el rango especificado
     */
    @Query("SELECT sal FROM SecurityAuditLog sal WHERE sal.userId = :userId " +
            "AND sal.timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY sal.timestamp DESC")
    Page<SecurityAuditLog> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Cuenta intentos fallidos de login de un usuario en un período
     * 
     * @param userId    ID del usuario
     * @param eventType Tipo de evento (LOGIN_FAILED)
     * @param since     Fecha desde la cual contar
     * @return Número de intentos fallidos
     */
    @Query("SELECT COUNT(sal) FROM SecurityAuditLog sal WHERE sal.userId = :userId " +
            "AND sal.eventType = :eventType AND sal.timestamp >= :since")
    long countFailedAttemptsSince(
            @Param("userId") Long userId,
            @Param("eventType") AuditEventType eventType,
            @Param("since") LocalDateTime since);

    /**
     * Encuentra los últimos N eventos de un usuario
     * 
     * @param userId   ID del usuario
     * @param pageable Configuración de paginación (usar PageRequest.of(0, N))
     * @return Lista de los últimos eventos del usuario
     */
    List<SecurityAuditLog> findTop10ByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Encuentra logs por dirección IP
     * 
     * @param ipAddress Dirección IP
     * @param pageable  Configuración de paginación
     * @return Página de logs desde la IP especificada
     */
    Page<SecurityAuditLog> findByIpAddressOrderByTimestampDesc(String ipAddress, Pageable pageable);
}
