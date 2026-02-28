package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, String> {
}
