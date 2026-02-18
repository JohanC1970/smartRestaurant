package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Service.CategoryService;
import com.smartRestaurant.inventory.Service.DishService;
import com.smartRestaurant.inventory.dto.Dish.CreateDishDTO;
import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import com.smartRestaurant.inventory.dto.Dish.UpdateDishDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Override
    public List<GetDishDTO> getAll() {
        return List.of();
    }

    @Override
    public void create(CreateDishDTO dish) {

    }

    @Override
    public void update(UpdateDishDTO dish) {

    }

    @Override
    public void delete(String id) {

    }
}
