package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.Addition;
import com.smartRestaurant.inventory.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdditionRepository extends JpaRepository<Addition, String> {

    Optional<Addition> findByName(String name);

    List<Addition> findByState(State state);

    @Query("SELECT COALESCE(SUM(a.units * a.purchasePrice), 0) FROM Addition a WHERE a.state = 'ACTIVE' AND a.additionType = 'SIMPLE' AND a.purchasePrice IS NOT NULL")
    Double calculateSimpleAdditionCapital();
}
