package com.smartRestaurant.inventory.dto.drink;

import com.smartRestaurant.inventory.model.Category;
import com.smartRestaurant.inventory.model.State;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record CreateDrinkDTO(
        @NotBlank @Length(min = 1, max = 50)
        String name,
         @NotBlank @Length(min = 10, max = 500)
         String description,
         @Positive @NotBlank
         double  mililiters,
         @NotBlank
         boolean alcohol,
         @NotNull @Length(min = 1, max = 3)
         List<String>photos,
         @NotBlank @Positive
         int units) {
}
