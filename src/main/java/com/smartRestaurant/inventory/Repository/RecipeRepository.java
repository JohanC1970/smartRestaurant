package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.dto.recipe.GetRecipeDTO;
import com.smartRestaurant.inventory.model.Recipe;
import com.smartRestaurant.inventory.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, String> {

    List<Recipe> findByDish_Id(String dishId);

    @Modifying
    @Query("UPDATE Recipe r SET r.state = :state WHERE r.dish.id = :dishId")
    void updateStateByDishId(String dishId, State state);
}
