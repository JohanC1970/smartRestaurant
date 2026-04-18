package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.CategoryRepository;
import com.smartRestaurant.inventory.Repository.DrinkRepository;
import com.smartRestaurant.inventory.Repository.NotificationRepository;
import com.smartRestaurant.inventory.Service.DrinkService;
import com.smartRestaurant.inventory.dto.drink.CreateDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.DrinkMovement;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDetailDTO;
import com.smartRestaurant.inventory.dto.drink.UpdateDrinkDTO;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.exceptions.ValueConflictException;
import com.smartRestaurant.inventory.mapper.DrinkMapper;
import com.smartRestaurant.inventory.model.Category;
import com.smartRestaurant.inventory.model.Drink;
import com.smartRestaurant.inventory.model.Notification;
import com.smartRestaurant.inventory.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DrinkServiceImpl implements DrinkService {

    private final DrinkRepository  drinkRepository;
    private final DrinkMapper drinkMapper;
    private final CategoryRepository categoryRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public List<GetDrinkDTO> getAll(int page) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Drink> drinks = drinkRepository.findAll(pageable);

        return drinks.stream()
                .filter(drink -> drink.getState().equals(State.ACTIVE))
                .map(drinkMapper::toDTO)
                .toList();
    }

    @Transactional
    @Override
    public void create(String categorieId, CreateDrinkDTO createDrinkDTO) {

        Optional<Category> optionalCategory = categoryRepository.findById(categorieId);
        if (optionalCategory.isEmpty() || optionalCategory.get().getState().equals(State.INACTIVE)) {
            throw new ResourceNotFoundException("No existe la categoría");
        }

        Optional<Drink> drink = drinkRepository.findByName(createDrinkDTO.name());
        if (drink.isPresent() && drink.get().getState().equals(State.ACTIVE)){
            throw new RuntimeException("Drink already exists");
        }

        Drink drinkEntity = drinkMapper.toEntity(createDrinkDTO);
        drinkEntity.setCategory(optionalCategory.get());

        drinkRepository.save(drinkEntity);
    }

    @Transactional
    @Override
    public void update(String id, UpdateDrinkDTO updateDrinkDTO) {
        Optional<Drink> drink = drinkRepository.findById(id);
        if (drink.isEmpty() || drink.get().getState().equals(State.INACTIVE)) {
            throw new RuntimeException("Drink does not exist");
        }
        drinkMapper.update(updateDrinkDTO, drink.get());
        drinkRepository.save(drink.get());
    }

    @Transactional
    @Override
    public void delete(String id) {
        Optional<Drink> drink = drinkRepository.findById(id);
        if (drink.isEmpty() || drink.get().getState().equals(State.INACTIVE)) {
            throw new RuntimeException("Drink does not exist");
        }
        drink.get().setState(State.INACTIVE);
        drinkRepository.save(drink.get());

    }

    @Override
    public GetDrinkDetailDTO getDrinkById(String id) {
        Optional<Drink> drink = drinkRepository.findById(id);
        if (drink.isEmpty() || drink.get().getState().equals(State.INACTIVE)) {
            throw new ResourceNotFoundException("Drink does not exist");
        }
        return drinkMapper.toDetailDTO(drink.get());
    }

    @Transactional
    @Override
    public void addStock(String id, DrinkMovement drinkMovement) {
        Drink drink = drinkRepository.findById(id)
                .filter(d -> !d.getState().equals(State.INACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException("Bebida no encontrada"));

        drink.setUnits(drink.getUnits() + drinkMovement.unit());
        drinkRepository.save(drink);

        checkAndNotifyLowStock(drink);
    }

    @Transactional
    @Override
    public void discountStock(String id, DrinkMovement drinkMovement) {
        Drink drink = drinkRepository.findById(id)
                .filter(d -> !d.getState().equals(State.INACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException("Bebida no encontrada"));

        int newUnits = drink.getUnits() - drinkMovement.unit();
        if (newUnits < 0) {
            throw new ValueConflictException(
                    "Stock insuficiente para '" + drink.getName() +
                    "': disponible=" + drink.getUnits() + ", requerido=" + drinkMovement.unit());
        }

        drink.setUnits(newUnits);
        drinkRepository.save(drink);

        checkAndNotifyLowStock(drink);
    }

    private void checkAndNotifyLowStock(Drink drink) {
        if (drink.getUnits() <= drink.getMinimumStock()) {
            Notification notification = Notification.builder()
                    .id(UUID.randomUUID().toString())
                    .type("Bajo nivel de stock de: " + drink.getName())
                    .createdAt(LocalDateTime.now())
                    .description("Revisar inventario. La bebida '" + drink.getName() +
                                 "' tiene " + drink.getUnits() + " unidades (mínimo: " +
                                 drink.getMinimumStock() + ")")
                    .build();
            notificationRepository.save(notification);
        }
    }

}
