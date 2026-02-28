package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.CategoryRepository;
import com.smartRestaurant.inventory.Repository.DrinkRepository;
import com.smartRestaurant.inventory.Service.DrinkService;
import com.smartRestaurant.inventory.dto.drink.CreateDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.UpdateDrinkDTO;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.mapper.DrinkMapper;
import com.smartRestaurant.inventory.model.Category;
import com.smartRestaurant.inventory.model.Drink;
import com.smartRestaurant.inventory.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DrinkServiceImpl implements DrinkService {

    private final DrinkRepository  drinkRepository;
    private final DrinkMapper drinkMapper;
    private final CategoryRepository categoryRepository;

    @Override
    public List<GetDrinkDTO> getAll(int page) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Drink> drinks = drinkRepository.findAll(pageable);

        if (drinks.getTotalElements() == 0) {
            throw new ResourceNotFoundException("No hay bebidas registradas");
        }

        return drinks.stream()
                .map(drinkMapper::toDTO)
                .toList();
    }

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

    @Override
    public GetDrinkDTO getDrinkById(String id) {
        Optional<Drink> drink = drinkRepository.findById(id);
        if (drink.isEmpty() || drink.get().getState().equals(State.INACTIVE)) {
            throw new ResourceNotFoundException("Drink does not exist");
        }
        return drinkMapper.toDTO(drink.get());
    }

}
