package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

}
