package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.DailyMenuRepository;
import com.smartRestaurant.inventory.Repository.DishRepository;
import com.smartRestaurant.inventory.Service.DailyMenuService;
import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.mapper.DailyMenuMapper;
import com.smartRestaurant.inventory.mapper.DishMapper;
import com.smartRestaurant.inventory.model.DailyMenu;
import com.smartRestaurant.inventory.model.Dish;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DailyMenuServiceImpl implements DailyMenuService {

    private final DailyMenuRepository dailyMenuRepository;
    private final DishRepository dishRepository;
    private final DailyMenuMapper dailyMenuMapper;
    private final DishMapper dishMapper;

    @Override
    public void add(String id) {
        Optional<Dish> optionalDish = dishRepository.findById(id);
        if (optionalDish.isEmpty()) {
            throw new ResourceNotFoundException("Plato no encontrado");
        }

        dailyMenuRepository.save(dailyMenuMapper.toEntity(optionalDish.get()));

    }

    @Override
    public List<GetDishDTO> getAll(int page) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Dish> dishPage = dailyMenuRepository.findDishes(pageable);

        if(dishPage.getTotalElements() == 0) {
            throw new ResourceNotFoundException("Platos no encontrados");
        }

        return dishPage.stream()
                .map(dishMapper::toDTO)
                .toList();
    }


    @Override
    public void delete(String id) {
        Optional<DailyMenu> optionalDailyMenu = dailyMenuRepository.findByDish_Id(id);

        if (optionalDailyMenu.isEmpty()) {
            throw new ResourceNotFoundException("Plato no encontrado");
        }
        dailyMenuRepository.delete(optionalDailyMenu.get());
    }
}
