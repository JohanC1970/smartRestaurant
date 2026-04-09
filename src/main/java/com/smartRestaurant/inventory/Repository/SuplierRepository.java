package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.Suplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SuplierRepository extends JpaRepository<Suplier, String> {

    Optional<Suplier> findByEmail(String email);

}
