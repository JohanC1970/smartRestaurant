package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.AdditionRecipeRepository;
import com.smartRestaurant.inventory.Repository.AdditionRepository;
import com.smartRestaurant.inventory.Repository.NotificationRepository;
import com.smartRestaurant.inventory.Repository.ProductRepository;
import com.smartRestaurant.inventory.Service.AdditionService;
import com.smartRestaurant.inventory.Service.InventoryMovementService;
import com.smartRestaurant.inventory.dto.Addition.AdditionRestockDTO;
import com.smartRestaurant.inventory.dto.Addition.CreateAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.CreateAdditionRecipeDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDetailDTO;
import com.smartRestaurant.inventory.dto.Addition.UpdateAdditionDTO;
import com.smartRestaurant.inventory.dto.drink.DrinkMovement;
import com.smartRestaurant.inventory.exceptions.BadRequestException;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.exceptions.ValueConflictException;
import com.smartRestaurant.inventory.mapper.AdditionMapper;
import com.smartRestaurant.inventory.model.Addition;
import com.smartRestaurant.inventory.model.AdditionRecipe;
import com.smartRestaurant.inventory.model.AdditionType;
import com.smartRestaurant.inventory.model.Notification;
import com.smartRestaurant.inventory.model.Product;
import com.smartRestaurant.inventory.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdditionServiceImpl implements AdditionService {

    private final AdditionRepository additionRepository;
    private final AdditionRecipeRepository additionRecipeRepository;
    private final AdditionMapper additionMapper;
    private final NotificationRepository notificationRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementService inventoryMovementService;

    @Override
    public List<GetAdditionDTO> getAll(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Addition> additions = additionRepository.findAll(pageable);

        return additions.stream()
                .filter(a -> a.getState().equals(State.ACTIVE))
                .map(additionMapper::toDto)
                .toList();
    }

    @Transactional
    @Override
    public void create(CreateAdditionDTO dto) {
        Optional<Addition> existing = additionRepository.findByName(dto.name());
        if (existing.isPresent() && existing.get().getState().equals(State.ACTIVE)) {
            throw new RuntimeException("Ya existe una adición activa con ese nombre");
        }

        validateCreateDTO(dto);

        Addition addition = additionMapper.toEntity(dto);

        if (dto.additionType() == AdditionType.SIMPLE) {
            addition.setUnits(dto.units() != null ? dto.units() : 0);
            addition.setMinimumStock(dto.minimumStock() != null ? dto.minimumStock() : 0);
            addition.setPurchasePrice(dto.purchasePrice());
        } else {
            // PREPARED: sin stock de unidades
            addition.setUnits(0);
            addition.setMinimumStock(0);
        }

        if (dto.photos() != null && !dto.photos().isEmpty()) {
            addition.setPhotos(dto.photos());
        }

        additionRepository.save(addition);

        if (dto.additionType() == AdditionType.PREPARED) {
            registerRecipes(dto.recipes(), addition);
        }
    }

    @Transactional
    @Override
    public void update(String id, UpdateAdditionDTO dto) {
        Addition addition = additionRepository.findById(id)
                .filter(a -> !a.getState().equals(State.INACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException("Adición no encontrada"));

        additionMapper.update(dto, addition);

        if (addition.getAdditionType() == AdditionType.SIMPLE) {
            if (dto.units() != null) addition.setUnits(dto.units());
            if (dto.minimumStock() != null) addition.setMinimumStock(dto.minimumStock());
        }

        if (addition.getAdditionType() == AdditionType.PREPARED && dto.recipes() != null && !dto.recipes().isEmpty()) {
            additionRecipeRepository.updateStateByAdditionId(id, State.INACTIVE);
            registerRecipes(dto.recipes(), addition);
        }

        if (dto.photos() != null) {
            addition.setPhotos(dto.photos());
        }

        additionRepository.save(addition);
    }

    @Transactional
    @Override
    public void delete(String id) {
        Addition addition = additionRepository.findById(id)
                .filter(a -> !a.getState().equals(State.INACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException("Adición no encontrada"));

        addition.setState(State.INACTIVE);

        if (addition.getAdditionType() == AdditionType.PREPARED) {
            additionRecipeRepository.updateStateByAdditionId(id, State.INACTIVE);
        }

        additionRepository.save(addition);
    }

    @Override
    public GetAdditionDetailDTO getById(String id) {
        Addition addition = additionRepository.findById(id)
                .filter(a -> !a.getState().equals(State.INACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException("Adición no encontrada"));

        return additionMapper.toDetailDTO(addition);
    }

    @Transactional
    @Override
    public void addStock(String id, AdditionRestockDTO dto) {
        Addition addition = additionRepository.findById(id)
                .filter(a -> !a.getState().equals(State.INACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException("Adición no encontrada"));

        if (addition.getAdditionType() == AdditionType.PREPARED) {
            throw new BadRequestException(
                "No se puede reabastecer una adición preparada por unidades. " +
                "Los ingredientes se gestionan desde el inventario de productos.");
        }

        addition.setUnits(addition.getUnits() + dto.unit());
        addition.setPurchasePrice(dto.purchasePrice());
        additionRepository.save(addition);

        inventoryMovementService.registerAdditionEntry(addition.getId(), addition.getName(), dto.unit(), dto.purchasePrice());

        checkAndNotifyLowStock(addition);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void discountStock(String id, DrinkMovement movement) {
        Addition addition = additionRepository.findById(id)
                .filter(a -> !a.getState().equals(State.INACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException("Adición no encontrada"));

        if (addition.getAdditionType() == AdditionType.PREPARED) {
            throw new BadRequestException(
                "Las adiciones preparadas no tienen stock de unidades. " +
                "Los ingredientes se descuentan directamente del inventario.");
        }

        int newUnits = addition.getUnits() - movement.unit();
        if (newUnits < 0) {
            throw new ValueConflictException(
                    "Stock insuficiente para '" + addition.getName() +
                    "': disponible=" + addition.getUnits() + ", requerido=" + movement.unit());
        }

        addition.setUnits(newUnits);
        additionRepository.save(addition);

        checkAndNotifyLowStock(addition);
    }

    private void registerRecipes(List<CreateAdditionRecipeDTO> recipes, Addition addition) {
        for (CreateAdditionRecipeDTO recipeDto : recipes) {
            Product product = productRepository.findById(recipeDto.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Ingrediente no encontrado: " + recipeDto.productId()));

            AdditionRecipe recipe = new AdditionRecipe();
            recipe.setId(UUID.randomUUID().toString());
            recipe.setAddition(addition);
            recipe.setProduct(product);
            recipe.setWeight(recipeDto.weight());
            recipe.setUnit(recipeDto.unit());
            recipe.setState(State.ACTIVE);
            additionRecipeRepository.save(recipe);
        }
    }

    private void validateCreateDTO(CreateAdditionDTO dto) {
        if (dto.additionType() == AdditionType.SIMPLE) {
            if (dto.purchasePrice() == null || dto.purchasePrice() <= 0) {
                throw new BadRequestException("El precio de compra es requerido para adiciones simples");
            }
            if (dto.units() == null || dto.units() < 1) {
                throw new BadRequestException("Las unidades iniciales son requeridas para adiciones simples");
            }
        } else if (dto.additionType() == AdditionType.PREPARED) {
            if (dto.recipes() == null || dto.recipes().isEmpty()) {
                throw new BadRequestException("Las adiciones preparadas requieren al menos una receta");
            }
        }
    }

    private void checkAndNotifyLowStock(Addition addition) {
        if (addition.getAdditionType() == AdditionType.PREPARED) return;

        if (addition.getUnits() <= addition.getMinimumStock()) {
            Notification notification = Notification.builder()
                    .id(UUID.randomUUID().toString())
                    .type("Bajo nivel de stock de: " + addition.getName())
                    .createdAt(LocalDateTime.now())
                    .description("Revisar inventario. La adición '" + addition.getName() +
                                 "' tiene " + addition.getUnits() + " unidades (mínimo: " +
                                 addition.getMinimumStock() + ")")
                    .build();
            notificationRepository.save(notification);
        }
    }
}
