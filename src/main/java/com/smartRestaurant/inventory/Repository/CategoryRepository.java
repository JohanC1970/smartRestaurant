package com.smartRestaurant.inventory.Repository;

import com.smartRestaurant.inventory.dto.Category.GetCategoriesDTO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository {

    List<GetCategoriesDTO> getAll();
}
