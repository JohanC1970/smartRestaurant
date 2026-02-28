package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.DailyMenu;
import com.smartRestaurant.inventory.model.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DailyMenuRepository extends JpaRepository<DailyMenu, String> {

    @Query("SELECT d.dish FROM DailyMenu d")
    Page<Dish> findDishes(Pageable pageable);

    Optional<DailyMenu> findByDish_Id(String dishId);


    //Page<Dish> findDishes(Pageable pageable);
}
