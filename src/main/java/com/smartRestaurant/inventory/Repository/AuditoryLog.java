package com.smartRestaurant.inventory.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoryLog extends JpaRepository<AuditoryLog, String> {
}
