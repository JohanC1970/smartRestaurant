package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.drink.CreateDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDetailDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkRecipeDTO;
import com.smartRestaurant.inventory.dto.drink.UpdateDrinkDTO;
import com.smartRestaurant.inventory.model.Drink;
import com.smartRestaurant.inventory.model.DrinkRecipe;
import com.smartRestaurant.inventory.model.DrinkType;
import com.smartRestaurant.inventory.model.State;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DrinkMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "state", constant = "ACTIVE")
    @Mapping(target = "reservedUnits", constant = "0")
    @Mapping(target = "recipes", ignore = true)
    Drink toEntity(CreateDrinkDTO createDrinkDTO);

    @Mapping(target = "photo", expression = "java(drink.getPhotos() != null && !drink.getPhotos().isEmpty() ? drink.getPhotos().get(0) : null)")
    @Mapping(target = "drinkType", expression = "java(drink.getDrinkType() != null ? drink.getDrinkType().name() : null)")
    @Mapping(target = "salePrice", source = "drink.salePrice")
    GetDrinkDTO toDTO(Drink drink);

    @Mapping(target = "photo", expression = "java(drink.getPhotos() != null && !drink.getPhotos().isEmpty() ? drink.getPhotos().get(0) : null)")
    @Mapping(target = "drinkType", expression = "java(drink.getDrinkType() != null ? drink.getDrinkType().name() : null)")
    @Mapping(target = "salePrice", source = "drink.salePrice")
    @Mapping(target = "purchasePrice", source = "drink.purchasePrice")
    @Mapping(target = "categoryId", source = "drink.category.id")
    @Mapping(target = "categoryName", source = "drink.category.name")
    @Mapping(target = "estimatedCost", expression = "java(calculateEstimatedCost(drink))")
    @Mapping(target = "margin", expression = "java(calculateMargin(drink))")
    @Mapping(target = "recipes", source = "drink.recipes")
    GetDrinkDetailDTO toDetailDTO(Drink drink);

    @Mapping(target = "productId", source = "recipe.product.id")
    @Mapping(target = "productName", source = "recipe.product.name")
    GetDrinkRecipeDTO toRecipeDTO(DrinkRecipe recipe);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "recipes", ignore = true)
    @Mapping(target = "purchasePrice", ignore = true)
    @Mapping(target = "drinkType", ignore = true)
    void update(UpdateDrinkDTO updateDrinkDTO, @MappingTarget Drink drink);

    default Double calculateEstimatedCost(Drink drink) {
        if (drink.getDrinkType() != DrinkType.PREPARED) return null;
        if (drink.getRecipes() == null || drink.getRecipes().isEmpty()) return null;
        double cost = drink.getRecipes().stream()
                .filter(r -> State.ACTIVE.equals(r.getState()))
                .mapToDouble(r -> r.getWeight() * r.getProduct().getPrice())
                .sum();
        return cost > 0 ? cost : null;
    }

    default Double calculateMargin(Drink drink) {
        Double cost = calculateEstimatedCost(drink);
        if (cost == null) return null;
        return drink.getSalePrice() - cost;
    }

}
