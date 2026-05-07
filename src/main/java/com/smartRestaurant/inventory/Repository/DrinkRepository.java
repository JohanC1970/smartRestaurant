package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.Drink;
import com.smartRestaurant.inventory.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrinkRepository extends JpaRepository<Drink,String> {

    Optional<Drink> findByName(String name);

    List<Drink> findByState(State state);

    @Query("SELECT COALESCE(SUM(d.units * d.purchasePrice), 0) FROM Drink d WHERE d.state = 'ACTIVE' AND d.drinkType = 'SIMPLE' AND d.purchasePrice IS NOT NULL")
    Double calculateSimpleDrinkCapital();
}
