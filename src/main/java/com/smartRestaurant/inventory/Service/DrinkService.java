package com.smartRestaurant.inventory.Service;

import com.smartRestaurant.inventory.dto.drink.CreateDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.DrinkMovement;
import com.smartRestaurant.inventory.dto.drink.DrinkRestockDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDTO;
import com.smartRestaurant.inventory.dto.drink.GetDrinkDetailDTO;
import com.smartRestaurant.inventory.dto.drink.UpdateDrinkDTO;

import java.util.List;

public interface DrinkService {

    List<GetDrinkDTO> getAll(int page);
    void create(String categorieId, CreateDrinkDTO createDrinkDTO);
    void update(String id, UpdateDrinkDTO updateDrinkDTO);
    void delete(String id);
    GetDrinkDetailDTO getDrinkById(String id);

    /**
     * Reabastecer una bebida SIMPLE: incrementa unidades y actualiza el precio de compra.
     * No aplica para bebidas PREPARED.
     */
    void addStock(String id, DrinkRestockDTO drinkRestockDTO);

    /**
     * Descontar unidades de una bebida SIMPLE (consumo por orden).
     * Para bebidas PREPARED el descuento de ingredientes lo gestiona OrderServiceImpl.
     */
    void discountStock(String id, DrinkMovement drinkMovement);

}
