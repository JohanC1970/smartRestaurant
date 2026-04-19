package com.smartRestaurant.restaurant.service;

import com.smartRestaurant.restaurant.dto.request.ChangeTableStatusDTO;
import com.smartRestaurant.restaurant.dto.request.CreateTableDTO;
import com.smartRestaurant.restaurant.dto.request.UpdateTableDTO;
import com.smartRestaurant.restaurant.dto.response.GetTableDTO;
import com.smartRestaurant.restaurant.model.enums.TableStatus;

import java.util.List;

public interface TableService {

    GetTableDTO create(CreateTableDTO dto);

    List<GetTableDTO> getAll(TableStatus status);

    GetTableDTO getById(String id);

    GetTableDTO update(String id, UpdateTableDTO dto);

    GetTableDTO changeStatus(String id, ChangeTableStatusDTO dto);

    void deactivate(String id);
}
