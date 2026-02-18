package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Service.CategoryService;
import com.smartRestaurant.inventory.dto.Category.CreateCategoryDTO;
import com.smartRestaurant.inventory.dto.Category.GetCategoriesDTO;
import com.smartRestaurant.inventory.dto.Category.UpdateCategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    @Override
    public List<GetCategoriesDTO> getAll() {
        return List.of();
    }

    @Override
    public void create(CreateCategoryDTO createCategoryDTO) {

    }

    @Override
    public void update(UpdateCategoryDTO updateCategoryDTO) {

    }

    @Override
    public void delete(String id) {

    }
}
