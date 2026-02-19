package com.smartRestaurant.inventory.Service;


import com.smartRestaurant.inventory.dto.Category.CreateCategoryDTO;
import com.smartRestaurant.inventory.dto.Category.GetCategoriesDTO;
import com.smartRestaurant.inventory.dto.Category.UpdateCategoryDTO;

import java.util.List;

public interface CategoryService {

    List<GetCategoriesDTO> getAll();
    void create(CreateCategoryDTO createCategoryDTO);
    void update(String id, UpdateCategoryDTO updateCategoryDTO);
    void delete(String id);
}
