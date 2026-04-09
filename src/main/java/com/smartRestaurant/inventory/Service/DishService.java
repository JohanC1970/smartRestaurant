package com.smartRestaurant.inventory.Service;

import com.smartRestaurant.inventory.dto.Dish.CreateDishDTO;
import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import com.smartRestaurant.inventory.dto.Dish.GetDishDetailDTO;
import com.smartRestaurant.inventory.dto.Dish.UpdateDishDTO;

import java.util.List;

public interface DishService {

    List<GetDishDTO> getAll(int page);
    void create(String categoryId, CreateDishDTO dish);
    void update(String id, UpdateDishDTO dish);
    void delete(String id);
    GetDishDetailDTO getById(String id);
}
