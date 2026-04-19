package com.smartRestaurant.restaurant.repository;

import com.smartRestaurant.restaurant.model.RestaurantTable;
import com.smartRestaurant.restaurant.model.enums.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, String> {

    boolean existsByNumber(int number);

    Optional<RestaurantTable> findByNumber(int number);

    List<RestaurantTable> findByActiveTrue();

    List<RestaurantTable> findByStatusAndActiveTrue(TableStatus status);
}
