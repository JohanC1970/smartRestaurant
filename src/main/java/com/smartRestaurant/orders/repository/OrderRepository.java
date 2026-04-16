package com.smartRestaurant.orders.repository;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.orders.model.Order;
import com.smartRestaurant.orders.model.enums.OrderChannel;
import com.smartRestaurant.orders.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByChannel(OrderChannel channel, Pageable pageable);

    Page<Order> findByStatusAndChannel(OrderStatus status, OrderChannel channel, Pageable pageable);

    Page<Order> findByCustomer(User customer, Pageable pageable);
}
