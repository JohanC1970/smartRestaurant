package com.smartRestaurant.inventory.Service;

import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface DailyMenuService {

    void add(String id);
    List<GetDishDTO> getAll(int page);
    void delete(String id);
}
