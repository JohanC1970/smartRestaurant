package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.InventoryMovement;
import com.smartRestaurant.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, String> {

    List<InventoryMovement> findByProduct(Product product);
    List<InventoryMovement> findByProductOrderByTimeAtDesc(Product product);
}
