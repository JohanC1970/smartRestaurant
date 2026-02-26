package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.DishRepository;
import com.smartRestaurant.inventory.Service.CategoryService;
import com.smartRestaurant.inventory.Service.DishService;
import com.smartRestaurant.inventory.dto.Dish.CreateDishDTO;
import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import com.smartRestaurant.inventory.dto.Dish.UpdateDishDTO;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.mapper.DishMapper;
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
public class DishServiceImpl implements DishService {

    private final DishRepository dishRepository;
    private final DishMapper dishMapper;


    // falta paginación
    @Override
    public List<GetDishDTO> getAll(int page) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Dish> dishes = dishRepository.findAll(pageable);

        if(dishes.getTotalElements() == 0){
            throw new ResourceNotFoundException("No hay platos registrados");
        }

        return dishes.stream()
                .map(dishMapper::toDTO)
                .toList();
    }

    @Override
    public void create(CreateDishDTO createDishDTO) {

        Optional<Dish> dish = dishRepository.findByName(createDishDTO.name());
        if (dish.isPresent()) {
            throw new RuntimeException("Already exists");
        }

        dishRepository.save(dishMapper.toEntity(createDishDTO));

    }

    @Override
    public void update(String id, UpdateDishDTO updateDishDTO) {
        Optional<Dish> dishOptional = dishRepository.findById(id);
        if (dishOptional.isEmpty()) {
            throw new RuntimeException("Not found");
        }

        dishMapper.updateDish(updateDishDTO, dishOptional.get());

        dishRepository.save(dishOptional.get());

    }


    // validar que un plato no esté pendiente de pago o que no afecte borrarlo
    @Override
    public void delete(String id) {

        Optional<Dish> dishOptional = dishRepository.findById(id);
        if (dishOptional.isEmpty() || dishOptional.get().getState().equals(State.INACTIVE)) {
            throw new RuntimeException("Not found, can´t be deleted");
        }

        dishOptional.get().setState(State.INACTIVE);
        dishRepository.save(dishOptional.get());
    }

    @Override
    public GetDishDTO getById(String id) {
        Optional<Dish> dish = dishRepository.findById(id);
        if(dish.isEmpty() || dish.get().getState().equals(State.INACTIVE)) {
            throw new ResourceNotFoundException("Dish not found");
        }

        return dishMapper.toDTO(dish.get());
    }
}
