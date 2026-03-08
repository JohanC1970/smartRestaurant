package com.smartRestaurant.inventory.dto.Category;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UpdateCategoryDTO(@NotBlank @Length(min = 1, max = 100)
                                String name,
                                @NotBlank @Length(min = 10, max = 500)
                                String description){

}
