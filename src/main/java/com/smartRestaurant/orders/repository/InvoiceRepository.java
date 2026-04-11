package com.smartRestaurant.orders.repository;

import com.smartRestaurant.orders.model.Invoice;
import com.smartRestaurant.orders.model.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
}

