package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.PublicationSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicationSectionRepository extends JpaRepository<PublicationSection, String> {
}
