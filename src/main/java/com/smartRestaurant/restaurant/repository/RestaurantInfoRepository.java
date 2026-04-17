package com.smartRestaurant.restaurant.repository;

import com.smartRestaurant.restaurant.model.RestaurantInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantInfoRepository extends JpaRepository<RestaurantInfo, String> {}
