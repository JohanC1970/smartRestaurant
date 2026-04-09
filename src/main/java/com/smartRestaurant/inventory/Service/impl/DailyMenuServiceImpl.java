package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.DailyMenuRepository;
import com.smartRestaurant.inventory.Repository.DishRepository;
import com.smartRestaurant.inventory.Service.DailyMenuService;
import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.exceptions.ValueConflictException;
import com.smartRestaurant.inventory.mapper.DailyMenuMapper;
import com.smartRestaurant.inventory.mapper.DishMapper;
import com.smartRestaurant.inventory.mapper.ShowDishesMappper;
import com.smartRestaurant.inventory.model.DailyMenu;
import com.smartRestaurant.inventory.model.Dish;
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
public class DailyMenuServiceImpl implements DailyMenuService {

    private final DailyMenuRepository dailyMenuRepository;
    private final DishRepository dishRepository;
    private final DailyMenuMapper dailyMenuMapper;
    private final DishMapper dishMapper;
    private final ShowDishesMappper showDishesMappper;

    @Override
    public void add(String id) {
        Optional<Dish> optionalDish = dishRepository.findById(id);
        if (optionalDish.isEmpty()) {
            throw new ResourceNotFoundException("Plato no encontrado");
        }

        // Verificar si el plato ya está en el menú diario
        if (dailyMenuRepository.existsByDish_Id(id)) {
            throw new ValueConflictException(
                "El plato ya está en el menú diario");
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
                .filter(dish -> dish.getState().equals(State.ACTIVE))
                .map(showDishesMappper::toDTO)
                .toList();
    }

    @Override
    public void delete(String id) {
        List<DailyMenu> dailyMenus = dailyMenuRepository.findByDish_Id(id);

        if (dailyMenus.isEmpty()) {
            throw new ResourceNotFoundException("Plato no encontrado en el menú diario");
        }
        // Eliminar todos los registros encontrados (incluyendo duplicados)
        dailyMenuRepository.deleteAll(dailyMenus);
    }
}
