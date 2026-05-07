package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.Dish;
import com.smartRestaurant.inventory.model.DishAvailability;
import com.smartRestaurant.inventory.model.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, String> {

    Optional<Dish> findByName(String name);

    Page<Dish> findByStateAndAvailabilityIn(
            State state, List<DishAvailability> availabilities, Pageable pageable);

    Page<Dish> findByStateAndAvailabilityInAndCategoryId(
            State state, List<DishAvailability> availabilities, String categoryId, Pageable pageable);

}
