package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.DrinkRepository;
import com.smartRestaurant.inventory.Service.DrinkService;
import com.smartRestaurant.inventory.dto.drink.CreateDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.UpdateDrinkDTO;
import com.smartRestaurant.inventory.mapper.DrinkMapper;
import com.smartRestaurant.inventory.model.Drink;
import com.smartRestaurant.inventory.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DrinkServiceImpl implements DrinkService {

    private final DrinkRepository  drinkRepository;
    private final DrinkMapper drinkMapper;

    @Override
    public List<GetDrinkDTO> getAll() {
        return drinkRepository.findAll().stream().map(drinkMapper::toDTO).toList();
    }

    @Override
    public void create(CreateDrinkDTO createDrinkDTO) {

        Optional<Drink> drink = drinkRepository.findByName(createDrinkDTO.name());
        if (drink.isPresent() && drink.get().getState().equals(State.ACTIVE)){
            throw new RuntimeException("Drink already exists");
        }
        drinkRepository.save(drinkMapper.toEntity(createDrinkDTO));
    }

    @Override
    public void update(String id, UpdateDrinkDTO updateDrinkDTO) {
        Optional<Drink> drink = drinkRepository.findById(id);
        if (drink.isEmpty() || drink.get().getState().equals(State.INACTIVE)) {
            throw new RuntimeException("Drink does not exist");
        }
        drinkMapper.update(updateDrinkDTO, drink.get());
        drinkRepository.save(drink.get());
    }

    @Override
    public void delete(String id) {
        Optional<Drink> drink = drinkRepository.findById(id);
        if (drink.isEmpty() || drink.get().getState().equals(State.INACTIVE)) {
            throw new RuntimeException("Drink does not exist");
        }
        drink.get().setState(State.INACTIVE);
        drinkRepository.save(drink.get());

    }
}
