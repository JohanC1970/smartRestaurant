package com.smartRestaurant.orders.repository;

import com.smartRestaurant.orders.model.Invoice;
import com.smartRestaurant.orders.model.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para operaciones CRUD de invoices
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    /**
     * Buscar factura por ID de orden
     */
    Optional<Invoice> findByOrderId(String orderId);

    /**
     * Buscar todas las facturas por estado
     */
    List<Invoice> findByStatus(InvoiceStatus status);

    /**
     * Buscar facturas por rango de fechas
     */
    List<Invoice> findByStatusAndCreatedAtBetween(
        InvoiceStatus status,
        LocalDateTime start,
        LocalDateTime end
    );

    // ── Dashboard ────────────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(i.total), 0.0) FROM Invoice i WHERE i.status = :status AND i.paidAt BETWEEN :start AND :end")
    Double sumRevenueByPeriod(@Param("status") InvoiceStatus status,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(AVG(i.total), 0.0) FROM Invoice i WHERE i.status = :status AND i.paidAt >= :start")
    Double avgTicketSince(@Param("status") InvoiceStatus status, @Param("start") LocalDateTime start);
}

