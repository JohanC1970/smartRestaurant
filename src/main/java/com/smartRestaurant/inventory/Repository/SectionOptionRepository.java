package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.SectionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionOptionRepository extends JpaRepository<SectionOption, String> {
}
