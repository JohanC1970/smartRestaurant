package com.smartRestaurant.orders.repository;

import com.smartRestaurant.orders.model.MenuOrderInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuOrderInstanceRepository extends JpaRepository<MenuOrderInstance, String> {
}
