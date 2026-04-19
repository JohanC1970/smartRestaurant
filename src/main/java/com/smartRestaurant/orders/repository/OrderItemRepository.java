package com.smartRestaurant.orders.repository;

import com.smartRestaurant.orders.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    // ── Dashboard ────────────────────────────────────────────────────────────

    @Query(nativeQuery = true, value = """
            SELECT d.id, d.name, d.price, SUM(oi.quantity) AS total_sold
            FROM order_item oi
            INNER JOIN dish d ON oi.producto_id = d.id
            INNER JOIN orders o ON oi.order_id = o.id
            WHERE oi.producto_type = 'DISH'
              AND o.status IN ('COMPLETED', 'DELIVERED')
              AND o.created_at >= :start
            GROUP BY d.id, d.name, d.price
            ORDER BY total_sold DESC
            LIMIT 5
            """)
    List<Object[]> findTop5DishesSince(@Param("start") LocalDateTime start);
}
