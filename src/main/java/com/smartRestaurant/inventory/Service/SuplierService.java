package com.smartRestaurant.inventory.Service;

import com.smartRestaurant.inventory.dto.Addition.CreateAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.GetAdditionDTO;
import com.smartRestaurant.inventory.dto.Addition.UpdateAdditionDTO;
import com.smartRestaurant.inventory.dto.Suplier.CreateSuplierDTO;
import com.smartRestaurant.inventory.dto.Suplier.GetSuplierDTO;
import com.smartRestaurant.inventory.dto.Suplier.UpdateSuplierDTO;

import java.util.List;

public interface SuplierService {

    List<GetSuplierDTO> getAll();
    void create(CreateSuplierDTO createSuplierDTO);
    void update(String id, UpdateSuplierDTO updateSuplierDTO);
    void delete(String id);
    GetSuplierDTO getById(String id);
}
