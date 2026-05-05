package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.Addition.CreateAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDetailDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionRecipeDTO;
import com.smartRestaurant.inventory.dto.Addition.UpdateAdditionDTO;
import com.smartRestaurant.inventory.model.Addition;
import com.smartRestaurant.inventory.model.AdditionRecipe;
import com.smartRestaurant.inventory.model.AdditionType;
import com.smartRestaurant.inventory.model.State;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AdditionMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "state", constant = "ACTIVE")
    @Mapping(target = "reservedUnits", constant = "0")
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "recipes", ignore = true)
    Addition toEntity(CreateAdditionDTO createAdditionDTO);

    @Mapping(target = "photo", expression = "java(addition.getPhotos() != null && !addition.getPhotos().isEmpty() ? addition.getPhotos().get(0) : null)")
    @Mapping(target = "additionType", expression = "java(addition.getAdditionType() != null ? addition.getAdditionType().name() : null)")
    @Mapping(target = "salePrice", source = "addition.salePrice")
    @Mapping(target = "availableUnits", expression = "java(addition.getUnits() - addition.getReservedUnits())")
    @Mapping(target = "state", expression = "java(addition.getState() != null ? addition.getState().name() : null)")
    GetAdditionDTO toDto(Addition addition);

    @Mapping(target = "additionType", expression = "java(addition.getAdditionType() != null ? addition.getAdditionType().name() : null)")
    @Mapping(target = "salePrice", source = "addition.salePrice")
    @Mapping(target = "purchasePrice", source = "addition.purchasePrice")
    @Mapping(target = "estimatedCost", expression = "java(calculateEstimatedCost(addition))")
    @Mapping(target = "margin", expression = "java(calculateMargin(addition))")
    @Mapping(target = "availableUnits", expression = "java(addition.getUnits() - addition.getReservedUnits())")
    @Mapping(target = "state", expression = "java(addition.getState() != null ? addition.getState().name() : null)")
    @Mapping(target = "recipes", source = "addition.recipes")
    GetAdditionDetailDTO toDetailDTO(Addition addition);

    @Mapping(target = "productId", source = "recipe.product.id")
    @Mapping(target = "productName", source = "recipe.product.name")
    GetAdditionRecipeDTO toRecipeDTO(AdditionRecipe recipe);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "reservedUnits", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "recipes", ignore = true)
    @Mapping(target = "purchasePrice", ignore = true)
    @Mapping(target = "additionType", ignore = true)
    void update(UpdateAdditionDTO updateAdditionDTO, @MappingTarget Addition addition);

    default Double calculateEstimatedCost(Addition addition) {
        if (addition.getAdditionType() != AdditionType.PREPARED) return null;
        if (addition.getRecipes() == null || addition.getRecipes().isEmpty()) return null;
        double cost = addition.getRecipes().stream()
                .filter(r -> State.ACTIVE.equals(r.getState()))
                .mapToDouble(r -> r.getWeight() * r.getProduct().getPrice())
                .sum();
        return cost > 0 ? cost : null;
    }

    default Double calculateMargin(Addition addition) {
        Double cost = calculateEstimatedCost(addition);
        if (cost == null) return null;
        return addition.getSalePrice() - cost;
    }
}
