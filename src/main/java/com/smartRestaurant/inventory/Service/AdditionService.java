package com.smartRestaurant.inventory.Service;

import com.smartRestaurant.inventory.dto.Addition.AdditionRestockDTO;
import com.smartRestaurant.inventory.dto.Addition.CreateAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDetailDTO;
import com.smartRestaurant.inventory.dto.Addition.UpdateAdditionDTO;
import com.smartRestaurant.inventory.dto.drink.DrinkMovement;

import java.util.List;

public interface AdditionService {

    List<GetAdditionDTO> getAll(int page);
    void create(CreateAdditionDTO createAdditionDTO);
    void update(String id, UpdateAdditionDTO updateAdditionDTO);
    void delete(String id);
    GetAdditionDetailDTO getById(String id);

    /**
     * Reabastece una adición SIMPLE: añade unidades y actualiza precio de compra.
     * Lanza BadRequestException si la adición es PREPARED.
     */
    void addStock(String id, AdditionRestockDTO dto);

    /**
     * Descuenta unidades de una adición SIMPLE.
     * Usado internamente por OrderServiceImpl para adiciones simples.
     */
    void discountStock(String id, DrinkMovement movement);
}
