package com.smartRestaurant.inventory.Service;

import com.smartRestaurant.inventory.dto.drink.CreateDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.UpdateDrinkDTO;

import java.util.List;

public interface DrinkService {

    List<GetDrinkDTO> getAll();
    void create(CreateDrinkDTO createDrinkDTO);
    void update(String id, UpdateDrinkDTO updateDrinkDTO);
    void delete(String id);
}
