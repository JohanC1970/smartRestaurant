package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.AdditionRecipe;
import com.smartRestaurant.inventory.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdditionRecipeRepository extends JpaRepository<AdditionRecipe, String> {

    List<AdditionRecipe> findByAddition_Id(String additionId);

    @Modifying
    @Query("UPDATE AdditionRecipe r SET r.state = :state WHERE r.addition.id = :additionId")
    void updateStateByAdditionId(String additionId, State state);

}
