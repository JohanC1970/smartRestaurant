package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.model.MenuPublication;
import com.smartRestaurant.inventory.model.MenuStatus;
import com.smartRestaurant.inventory.model.MenuTimeSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MenuPublicationRepository extends JpaRepository<MenuPublication, String> {

    Page<MenuPublication> findAllByOrderByDateDesc(Pageable pageable);

    Optional<MenuPublication> findByDateAndTimeSlotAndStatus(LocalDate date, MenuTimeSlot timeSlot, MenuStatus status);

    boolean existsByDateAndTimeSlotAndStatusNot(LocalDate date, MenuTimeSlot timeSlot, MenuStatus excludedStatus);
}
