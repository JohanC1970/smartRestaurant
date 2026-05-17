package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.InventoryMovement;
import com.smartRestaurant.inventory.model.Product;
import com.smartRestaurant.inventory.model.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, String> {

    List<InventoryMovement> findByProduct(Product product);
    List<InventoryMovement> findByProductOrderByTimeAtDesc(Product product);

    @Query("SELECT COALESCE(SUM(m.totalCost), 0.0) FROM InventoryMovement m WHERE m.type = :type AND m.timeAt BETWEEN :start AND :end")
    Double sumTotalCostByTypeAndPeriod(@Param("type") Type type,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);
}
