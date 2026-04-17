package com.smartRestaurant.orders.repository;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.orders.model.Order;
import com.smartRestaurant.orders.model.enums.OrderChannel;
import com.smartRestaurant.orders.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByChannel(OrderChannel channel, Pageable pageable);

    Page<Order> findByStatusAndChannel(OrderStatus status, OrderChannel channel, Pageable pageable);

    Page<Order> findByCustomer(User customer, Pageable pageable);

    /**
     * Busca órdenes online en estado PENDING (pago pendiente) creadas antes de una fecha dada.
     * Se usa para limpiar automáticamente las órdenes abandonadas en la pasarela de pago.
     */
    @Query("SELECT o FROM Order o WHERE o.channel = 'ONLINE' AND o.paymentStatus = 'PENDING' AND o.status = 'PENDING' AND o.createdAt < :expirationTime")
    List<Order> findAbandonedOnlineOrders(@Param("expirationTime") LocalDateTime expirationTime);

    // ── Dashboard ────────────────────────────────────────────────────────────

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses")
    long countByStatusIn(@Param("statuses") Collection<OrderStatus> statuses);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :start")
    long countSince(@Param("start") LocalDateTime start);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.createdAt >= :start")
    long countByStatusSince(@Param("status") OrderStatus status, @Param("start") LocalDateTime start);
}
