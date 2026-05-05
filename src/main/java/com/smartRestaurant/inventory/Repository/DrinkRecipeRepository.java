package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.DrinkRecipe;
import com.smartRestaurant.inventory.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrinkRecipeRepository extends JpaRepository<DrinkRecipe, String> {

    List<DrinkRecipe> findByDrink_Id(String drinkId);

    @Modifying
    @Query("UPDATE DrinkRecipe r SET r.state = :state WHERE r.drink.id = :drinkId")
    void updateStateByDrinkId(String drinkId, State state);

}
