package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.Addition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdditionRepository extends JpaRepository<Addition, String> {

    Optional<Addition> findByName(String name);
}
