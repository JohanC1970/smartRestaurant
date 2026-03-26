package com.smartRestaurant.orders.repository;

import com.smartRestaurant.orders.model.Payment;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymenRepository extends JpaRepository<Payment, String> {
}
