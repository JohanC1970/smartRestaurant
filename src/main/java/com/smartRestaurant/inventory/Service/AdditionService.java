package com.smartRestaurant.inventory.Service;

import com.smartRestaurant.inventory.dto.Addition.CreateAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.UpdateAdditionDTO;
import com.smartRestaurant.inventory.model.Addition;

import java.util.List;

public interface AdditionService {

    List<GetAdditionDTO> getAll(int page);
    void create(CreateAdditionDTO createAdditionDTO);
    void update(String id, UpdateAdditionDTO updateAdditionDTO);
    void delete(String id);
    GetAdditionDTO getById(String id);
}
