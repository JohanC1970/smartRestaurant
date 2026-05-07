package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.MenuTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuTemplateRepository extends JpaRepository<MenuTemplate, String> {
    boolean existsByName(String name);
}
