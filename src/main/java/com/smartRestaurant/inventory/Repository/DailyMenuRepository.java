package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.DailyMenu;
import com.smartRestaurant.inventory.model.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DailyMenuRepository extends JpaRepository<DailyMenu, String> {

    @Query("SELECT d.dish FROM DailyMenu d")
    Page<Dish> findDishes(Pageable pageable);

    // Retorna lista para manejar duplicados
    List<DailyMenu> findByDish_Id(String dishId);

    // Verifica si un plato ya está en el menú diario
    boolean existsByDish_Id(String dishId);

    // Elimina todos los registros de un plato del menú diario
    void deleteByDish_Id(String dishId);
}
