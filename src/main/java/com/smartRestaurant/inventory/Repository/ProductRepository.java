package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findByName(String name);

    // ── Dashboard ────────────────────────────────────────────────────────────

    @Query(nativeQuery = true, value = """
            SELECT
                p.id,
                p.name,
                p.minimum_stock,
                COALESCE(SUM(CASE WHEN im.type = 'ENTRY' THEN im.weight ELSE -im.weight END), 0) AS current_stock
            FROM product p
            LEFT JOIN inventory_movement im ON im.product_id = p.id
            WHERE p.state = 'ACTIVE'
            GROUP BY p.id, p.name, p.minimum_stock
            HAVING COALESCE(SUM(CASE WHEN im.type = 'ENTRY' THEN im.weight ELSE -im.weight END), 0) < p.minimum_stock
            ORDER BY current_stock ASC
            """)
    List<Object[]> findProductsBelowMinimumStock();
}
