package com.smartRestaurant.inventory.dto.Dish;

import com.smartRestaurant.inventory.model.Addition;
import com.smartRestaurant.inventory.model.Category;
import com.smartRestaurant.inventory.model.Product;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record CreateDishDTO(@NotBlank @Length(min = 1, max = 100)
                            String name,
                            @NotBlank @Length(min = 10, max = 100)
                            String description,
                            @Positive @NotNull
                            double price,
                            @NotEmpty @Size(min = 1, max = 10)
                            List<String> photos) {
}
