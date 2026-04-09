package com.smartRestaurant.orders.repository;

import com.smartRestaurant.orders.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository <Order, String>{
}
