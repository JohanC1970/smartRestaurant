package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.CategoryRepository;
import com.smartRestaurant.inventory.Service.CategoryService;
import com.smartRestaurant.inventory.dto.Category.CreateCategoryDTO;
import com.smartRestaurant.inventory.dto.Category.GetCategoriesDTO;
import com.smartRestaurant.inventory.dto.Category.UpdateCategoryDTO;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.mapper.CategoryMapper;
import com.smartRestaurant.inventory.model.Category;
import com.smartRestaurant.inventory.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // falta paginación
    @Override
    public List<GetCategoriesDTO> getAll() {

        return categoryRepository.findAll().stream()
                .filter(category -> category.getState().equals(State.ACTIVE))
                .map(categoryMapper::toDTO)
                .toList();
    }

    @Transactional
    @Override
    public void create(CreateCategoryDTO createCategoryDTO) {

        Optional<Category> category = categoryRepository.findByName(createCategoryDTO.name());

        if(category.isPresent() && category.get().getState().equals(State.ACTIVE)){
            throw new RuntimeException("La categoría ya existe");
        }

        categoryRepository.save(categoryMapper.toEntity(createCategoryDTO));

    }

    @Transactional
    @Override
    public void update(String id, UpdateCategoryDTO updateCategoryDTO) {

        Optional<Category> category = categoryRepository.findById(id);
        if(category.isEmpty() || category.get().getState().equals(State.INACTIVE)){
            throw new RuntimeException("Categoria no encontrada");
        }

        categoryMapper.update(updateCategoryDTO, category.get() );

        categoryRepository.save(category.get());

    }

    @Transactional
    @Override
    public void delete(String id) {

        Optional<Category> category = categoryRepository.findById(id);
        if(category.isEmpty()){
            throw new RuntimeException("Categoria no encontrada");
        }

        // Falta validar que si tiene platos dentro como se hará para inhabilitar la categoría
        category.get().setState(State.INACTIVE);

        categoryRepository.save(category.get());
    }

    @Override
    public GetCategoriesDTO getCategoryById(String id) {

        Optional<Category> category = categoryRepository.findById(id);
        if(category.isEmpty() ||  category.get().getState().equals(State.INACTIVE)){
            throw new ResourceNotFoundException("Categoria no encontrada");
        }

        return categoryMapper.toDTO(category.get());
    }
}
