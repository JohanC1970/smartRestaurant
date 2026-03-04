package com.smartRestaurant.inventory.Service;

import com.smartRestaurant.inventory.dto.drink.CreateDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDetailDTO;
import com.smartRestaurant.inventory.dto.drink.UpdateDrinkDTO;

import java.util.List;

public interface DrinkService {

    List<GetDrinkDTO> getAll(int page);
    void create(String categorieId, CreateDrinkDTO createDrinkDTO);
    void update(String id, UpdateDrinkDTO updateDrinkDTO);
    void delete(String id);
    GetDrinkDetailDTO getDrinkById(String id);
}
