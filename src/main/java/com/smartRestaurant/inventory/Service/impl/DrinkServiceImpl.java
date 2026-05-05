package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.CategoryRepository;
import com.smartRestaurant.inventory.Repository.DrinkRecipeRepository;
import com.smartRestaurant.inventory.Repository.DrinkRepository;
import com.smartRestaurant.inventory.Repository.NotificationRepository;
import com.smartRestaurant.inventory.Repository.ProductRepository;
import com.smartRestaurant.inventory.Service.DrinkService;
import com.smartRestaurant.inventory.Service.InventoryMovementService;
import com.smartRestaurant.inventory.dto.drink.CreateDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.CreateDrinkRecipeDTO;
import com.smartRestaurant.inventory.dto.drink.DrinkMovement;
import com.smartRestaurant.inventory.dto.drink.DrinkRestockDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDetailDTO;
import com.smartRestaurant.inventory.dto.drink.UpdateDrinkDTO;
import com.smartRestaurant.inventory.exceptions.BadRequestException;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.exceptions.ValueConflictException;
import com.smartRestaurant.inventory.mapper.DrinkMapper;
import com.smartRestaurant.inventory.model.Category;
import com.smartRestaurant.inventory.model.Drink;
import com.smartRestaurant.inventory.model.DrinkRecipe;
import com.smartRestaurant.inventory.model.DrinkType;
import com.smartRestaurant.inventory.model.Notification;
import com.smartRestaurant.inventory.model.Product;
import com.smartRestaurant.inventory.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DrinkServiceImpl implements DrinkService {

    private final DrinkRepository drinkRepository;
    private final DrinkRecipeRepository drinkRecipeRepository;
    private final DrinkMapper drinkMapper;
    private final CategoryRepository categoryRepository;
    private final NotificationRepository notificationRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementService inventoryMovementService;

    @Override
    public List<GetDrinkDTO> getAll(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Drink> drinks = drinkRepository.findAll(pageable);

        return drinks.stream()
                .filter(drink -> drink.getState().equals(State.ACTIVE))
                .map(drinkMapper::toDTO)
                .toList();
    }

    @Transactional
    @Override
    public void create(String categorieId, CreateDrinkDTO dto) {
        Category category = categoryRepository.findById(categorieId)
                .filter(c -> !c.getState().equals(State.INACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException("No existe la categoría"));

        Optional<Drink> existing = drinkRepository.findByName(dto.name());
        if (existing.isPresent() && existing.get().getState().equals(State.ACTIVE)) {
            throw new RuntimeException("Ya existe una bebida activa con ese nombre");
        }

        validateCreateDTO(dto);

        Drink drink = drinkMapper.toEntity(dto);
        drink.setCategory(category);

        if (dto.drinkType() == DrinkType.SIMPLE) {
            drink.setUnits(dto.units() != null ? dto.units() : 0);
            drink.setMinimumStock(dto.minimumStock() != null ? dto.minimumStock() : 0);
        } else {
            // PREPARED: sin stock de unidades
            drink.setUnits(0);
            drink.setMinimumStock(0);
        }

        drinkRepository.save(drink);

        if (dto.drinkType() == DrinkType.PREPARED) {
            registerRecipes(dto.recipes(), drink);
        }
    }

    @Transactional
    @Override
    public void update(String id, UpdateDrinkDTO dto) {
        Drink drink = drinkRepository.findById(id)
                .filter(d -> !d.getState().equals(State.INACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException("Bebida no encontrada"));

        drinkMapper.update(dto, drink);

        if (drink.getDrinkType() == DrinkType.SIMPLE) {
            if (dto.units() != null) drink.setUnits(dto.units());
            if (dto.minimumStock() != null) drink.setMinimumStock(dto.minimumStock());
        }

        if (drink.getDrinkType() == DrinkType.PREPARED && dto.recipes() != null && !dto.recipes().isEmpty()) {
            drinkRecipeRepository.updateStateByDrinkId(id, State.INACTIVE);
            registerRecipes(dto.recipes(), drink);
        }

        drinkRepository.save(drink);
    }

    @Transactional
    @Override
    public void delete(String id) {
        Drink drink = drinkRepository.findById(id)
                .filter(d -> !d.getState().equals(State.INACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException("Bebida no encontrada"));

        drink.setState(State.INACTIVE);

        if (drink.getDrinkType() == DrinkType.PREPARED) {
            drinkRecipeRepository.updateStateByDrinkId(id, State.INACTIVE);
        }

        drinkRepository.save(drink);
    }

    @Override
    public GetDrinkDetailDTO getDrinkById(String id) {
        Drink drink = drinkRepository.findById(id)
                .filter(d -> !d.getState().equals(State.INACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException("Bebida no encontrada"));

        return drinkMapper.toDetailDTO(drink);
    }

    @Transactional
    @Override
    public void addStock(String id, DrinkRestockDTO dto) {
        Drink drink = drinkRepository.findById(id)
                .filter(d -> !d.getState().equals(State.INACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException("Bebida no encontrada"));

        if (drink.getDrinkType() == DrinkType.PREPARED) {
            throw new BadRequestException(
                "No se puede reabastecer una bebida preparada por unidades. " +
                "Los ingredientes se gestionan desde el inventario de productos.");
        }

        drink.setUnits(drink.getUnits() + dto.unit());
        drink.setPurchasePrice(dto.purchasePrice());
        drinkRepository.save(drink);

        inventoryMovementService.registerDrinkEntry(drink.getId(), drink.getName(), dto.unit(), dto.purchasePrice());

        checkAndNotifyLowStock(drink);
    }

    @Transactional
    @Override
    public void discountStock(String id, DrinkMovement drinkMovement) {
        Drink drink = drinkRepository.findById(id)
                .filter(d -> !d.getState().equals(State.INACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException("Bebida no encontrada"));

        if (drink.getDrinkType() == DrinkType.PREPARED) {
            throw new BadRequestException(
                "Las bebidas preparadas no tienen stock de unidades. " +
                "Los ingredientes se descuentan directamente del inventario.");
        }

        int newUnits = drink.getUnits() - drinkMovement.unit();
        if (newUnits < 0) {
            throw new ValueConflictException(
                    "Stock insuficiente para '" + drink.getName() +
                    "': disponible=" + drink.getUnits() + ", requerido=" + drinkMovement.unit());
        }

        drink.setUnits(newUnits);
        drinkRepository.save(drink);

        checkAndNotifyLowStock(drink);
    }

    private void registerRecipes(List<CreateDrinkRecipeDTO> recipes, Drink drink) {
        for (CreateDrinkRecipeDTO recipeDto : recipes) {
            Product product = productRepository.findById(recipeDto.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingrediente no encontrado: " + recipeDto.productId()));

            DrinkRecipe recipe = new DrinkRecipe();
            recipe.setId(UUID.randomUUID().toString());
            recipe.setDrink(drink);
            recipe.setProduct(product);
            recipe.setWeight(recipeDto.weight());
            recipe.setUnit(recipeDto.unit());
            recipe.setState(State.ACTIVE);
            drinkRecipeRepository.save(recipe);
        }
    }

    private void validateCreateDTO(CreateDrinkDTO dto) {
        if (dto.drinkType() == DrinkType.SIMPLE) {
            if (dto.purchasePrice() == null || dto.purchasePrice() <= 0) {
                throw new BadRequestException("El precio de compra es requerido para bebidas simples");
            }
            if (dto.units() == null || dto.units() < 1) {
                throw new BadRequestException("Las unidades iniciales son requeridas para bebidas simples");
            }
        } else if (dto.drinkType() == DrinkType.PREPARED) {
            if (dto.recipes() == null || dto.recipes().isEmpty()) {
                throw new BadRequestException("Las bebidas preparadas requieren al menos una receta");
            }
        }
    }

    private void checkAndNotifyLowStock(Drink drink) {
        if (drink.getDrinkType() == DrinkType.PREPARED) return;

        if (drink.getUnits() <= drink.getMinimumStock()) {
            Notification notification = Notification.builder()
                    .id(UUID.randomUUID().toString())
                    .type("Bajo nivel de stock de: " + drink.getName())
                    .createdAt(LocalDateTime.now())
                    .description("Revisar inventario. La bebida '" + drink.getName() +
                                 "' tiene " + drink.getUnits() + " unidades (mínimo: " +
                                 drink.getMinimumStock() + ")")
                    .build();
            notificationRepository.save(notification);
        }
    }

}
