package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.CategoryRepository;
import com.smartRestaurant.inventory.Repository.DishRepository;
import com.smartRestaurant.inventory.Repository.RecipeRepository;
import com.smartRestaurant.inventory.Service.CategoryService;
import com.smartRestaurant.inventory.Service.DishService;
import com.smartRestaurant.inventory.Service.RecipeService;
import com.smartRestaurant.inventory.dto.Dish.CreateDishDTO;
import com.smartRestaurant.inventory.dto.Dish.GetDishDTO;
import com.smartRestaurant.inventory.dto.Dish.GetDishDetailDTO;
import com.smartRestaurant.inventory.dto.Dish.UpdateDishDTO;
import com.smartRestaurant.inventory.dto.recipe.GetRecipeDTO;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.mapper.DishMapper;
import com.smartRestaurant.inventory.mapper.ShowDishesMappper;
import com.smartRestaurant.inventory.model.Category;
import com.smartRestaurant.inventory.model.Dish;
import com.smartRestaurant.inventory.model.DishAvailability;
import com.smartRestaurant.inventory.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishRepository dishRepository;
    private final DishMapper dishMapper;
    private final CategoryRepository categoryRepository;
    private final RecipeService recipeService;
    private final ShowDishesMappper showDishesMappper;
    private final RecipeRepository recipeRepository;


    @Override
    public List<GetDishDTO> getAll(int page, String categoryId, boolean customerView, boolean menuEligible) {
        Pageable pageable = PageRequest.of(page, 10);

        // menuEligible: solo MENU_DEL_DIA + BOTH (para armar publicaciones del menú del día)
        // customerView: solo REGULAR + BOTH (para el tab de Platos del cliente/mesero)
        // ninguno:      todos (para el inventario del admin/cocina)
        List<DishAvailability> allowedAvailabilities;
        if (menuEligible) {
            allowedAvailabilities = List.of(DishAvailability.MENU_DEL_DIA, DishAvailability.BOTH);
        } else if (customerView) {
            allowedAvailabilities = List.of(DishAvailability.REGULAR, DishAvailability.BOTH);
        } else {
            allowedAvailabilities = List.of(DishAvailability.values());
        }

        Page<Dish> dishes = categoryId != null && !categoryId.isBlank()
                ? dishRepository.findByStateAndAvailabilityInAndCategoryId(
                        State.ACTIVE, allowedAvailabilities, categoryId, pageable)
                : dishRepository.findByStateAndAvailabilityIn(
                        State.ACTIVE, allowedAvailabilities, pageable);

        return dishes.stream()
                .map(showDishesMappper::toDTO)
                .toList();
    }

    @Transactional
    @Override
    public void create(String categoryId, CreateDishDTO createDishDTO) {

        Optional<Category> category = categoryRepository.findById(categoryId);

        if(category.isEmpty() || category.get().getState().equals(State.INACTIVE)){
            throw new ResourceNotFoundException("No existe esta categoría");
        }

        Optional<Dish> optionalDish = dishRepository.findByName(createDishDTO.name());
        if (optionalDish.isPresent()) {
            throw new RuntimeException("Ya existe este plato");
        }

        Dish dish = dishMapper.toEntity(createDishDTO);
        dish.setCategory(category.get());

        // Primero guardamos el dish para que tenga un ID
        dishRepository.save(dish);

        // Luego registramos las recipes con el dish ya persistido
        recipeService.registerRecipe(createDishDTO.ingredients(), dish);

    }

    @Transactional
    @Override
    public void update(String id, UpdateDishDTO updateDishDTO) {
        Optional<Dish> dishOptional = dishRepository.findById(id);
        if (dishOptional.isEmpty() || dishOptional.get().getState().equals(State.INACTIVE)) {
            throw new RuntimeException("No se encuentra el plato");
        }

        Optional<Category> category = categoryRepository.findById(updateDishDTO.categoryId());
        if (category.isEmpty() || category.get().getState().equals(State.INACTIVE)) {
            throw new ResourceNotFoundException("No existe esta categoría");
        }

        dishMapper.updateDish(updateDishDTO, dishOptional.get());
        dishOptional.get().setCategory(category.get());

        recipeRepository.updateStateByDishId(id, State.INACTIVE);
        recipeService.registerRecipe(updateDishDTO.ingredients(), dishOptional.get());

        dishRepository.save(dishOptional.get());
    }

    // validar que un plato no esté pendiente de pago o que no afecte borrarlo
    @Transactional
    @Override
    public void delete(String id) {

        Optional<Dish> dishOptional = dishRepository.findById(id);
        if (dishOptional.isEmpty() || dishOptional.get().getState().equals(State.INACTIVE)) {
            throw new RuntimeException("No existe, no puede ser eliminado");
        }

        // Primero deshabilitamos las recipes asociadas al plato
        recipeRepository.updateStateByDishId(id, State.INACTIVE);

        // Luego deshabilitamos el plato
        dishOptional.get().setState(State.INACTIVE);
        dishRepository.save(dishOptional.get());
    }

    @Override
    public GetDishDetailDTO getById(String id) {
        Optional<Dish> dish = dishRepository.findById(id);

        if(dish.isEmpty() || dish.get().getState().equals(State.INACTIVE)) {
            throw new ResourceNotFoundException("Plato no encontrado");
        }

        return dishMapper.toDTO(dish.get());

    }
}
