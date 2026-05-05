package com.smartRestaurant.accounting.service.impl;

import com.smartRestaurant.accounting.dto.AccountingSummaryDTO;
import com.smartRestaurant.accounting.dto.AdditionCostDTO;
import com.smartRestaurant.accounting.dto.DishCostDTO;
import com.smartRestaurant.accounting.dto.DrinkCostDTO;
import com.smartRestaurant.accounting.service.AccountingService;
import com.smartRestaurant.inventory.Repository.AdditionRepository;
import com.smartRestaurant.inventory.Repository.DishRepository;
import com.smartRestaurant.inventory.Repository.DrinkRepository;
import com.smartRestaurant.inventory.Repository.InventoryMovementRepository;
import com.smartRestaurant.inventory.Repository.ProductRepository;
import com.smartRestaurant.inventory.model.Addition;
import com.smartRestaurant.inventory.model.AdditionType;
import com.smartRestaurant.inventory.model.Dish;
import com.smartRestaurant.inventory.model.Drink;
import com.smartRestaurant.inventory.model.DrinkType;
import com.smartRestaurant.inventory.model.State;
import com.smartRestaurant.inventory.model.Type;
import com.smartRestaurant.orders.model.enums.InvoiceStatus;
import com.smartRestaurant.orders.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountingServiceImpl implements AccountingService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final DishRepository dishRepository;
    private final AdditionRepository additionRepository;
    private final DrinkRepository drinkRepository;

    @Override
    public AccountingSummaryDTO getSummary(LocalDateTime from, LocalDateTime to) {
        Double rawExpenses = inventoryMovementRepository.sumTotalCostByTypeAndPeriod(Type.ENTRY, from, to);
        double expenses = rawExpenses != null ? rawExpenses : 0.0;

        Double rawRevenue = invoiceRepository.sumRevenueByPeriod(InvoiceStatus.PAID, from, to);
        double revenue = rawRevenue != null ? rawRevenue : 0.0;

        Double rawCapital = productRepository.calculateInventoryCapital();
        double capital = rawCapital != null ? rawCapital : 0.0;

        Double rawAdditionCapital = additionRepository.calculateSimpleAdditionCapital();
        double additionCapital = rawAdditionCapital != null ? rawAdditionCapital : 0.0;

        Double rawDrinkCapital = drinkRepository.calculateSimpleDrinkCapital();
        double drinkCapital = rawDrinkCapital != null ? rawDrinkCapital : 0.0;

        List<DishCostDTO> dishCosts = dishRepository.findAll().stream()
                .filter(d -> d.getState() == State.ACTIVE)
                .map(this::toDishCostDTO)
                .toList();

        List<DrinkCostDTO> drinkCosts = drinkRepository.findByState(State.ACTIVE).stream()
                .map(this::toDrinkCostDTO)
                .toList();

        List<AdditionCostDTO> additionCosts = additionRepository.findByState(State.ACTIVE).stream()
                .map(this::toAdditionCostDTO)
                .toList();

        return AccountingSummaryDTO.builder()
                .totalExpenses(expenses)
                .totalRevenue(revenue)
                .inventoryCapital(capital)
                .drinkCapital(drinkCapital)
                .additionCapital(additionCapital)
                .estimatedProfit(revenue - expenses)
                .dishCosts(dishCosts)
                .drinkCosts(drinkCosts)
                .additionCosts(additionCosts)
                .build();
    }

    private DishCostDTO toDishCostDTO(Dish dish) {
        double cost = dish.getRecipes() == null ? 0.0 :
                dish.getRecipes().stream()
                        .mapToDouble(r -> r.getWeight() * r.getProduct().getPrice())
                        .sum();
        return DishCostDTO.builder()
                .dishId(dish.getId())
                .dishName(dish.getName())
                .dishPrice(dish.getPrice())
                .estimatedCost(cost)
                .margin(dish.getPrice() - cost)
                .build();
    }

    private DrinkCostDTO toDrinkCostDTO(Drink drink) {
        double estimatedCost;
        if (drink.getDrinkType() == DrinkType.SIMPLE) {
            estimatedCost = drink.getPurchasePrice() != null ? drink.getPurchasePrice() : 0.0;
        } else {
            estimatedCost = drink.getRecipes() == null ? 0.0 :
                    drink.getRecipes().stream()
                            .filter(r -> State.ACTIVE.equals(r.getState()))
                            .mapToDouble(r -> r.getWeight() * r.getProduct().getPrice())
                            .sum();
        }
        return DrinkCostDTO.builder()
                .drinkId(drink.getId())
                .drinkName(drink.getName())
                .drinkType(drink.getDrinkType().name())
                .salePrice(drink.getSalePrice())
                .purchasePrice(drink.getPurchasePrice())
                .estimatedCost(estimatedCost)
                .margin(drink.getSalePrice() - estimatedCost)
                .units(drink.getUnits())
                .build();
    }

    private AdditionCostDTO toAdditionCostDTO(Addition addition) {
        double estimatedCost;
        if (addition.getAdditionType() == AdditionType.SIMPLE) {
            estimatedCost = addition.getPurchasePrice() != null ? addition.getPurchasePrice() : 0.0;
        } else {
            estimatedCost = addition.getRecipes() == null ? 0.0 :
                    addition.getRecipes().stream()
                            .filter(r -> State.ACTIVE.equals(r.getState()))
                            .mapToDouble(r -> r.getWeight() * r.getProduct().getPrice())
                            .sum();
        }
        return AdditionCostDTO.builder()
                .additionId(addition.getId())
                .additionName(addition.getName())
                .additionType(addition.getAdditionType().name())
                .salePrice(addition.getSalePrice())
                .purchasePrice(addition.getPurchasePrice())
                .estimatedCost(estimatedCost)
                .margin(addition.getSalePrice() - estimatedCost)
                .units(addition.getUnits())
                .build();
    }
}
