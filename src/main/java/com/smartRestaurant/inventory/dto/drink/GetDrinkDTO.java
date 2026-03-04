package com.smartRestaurant.inventory.dto.drink;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record GetDrinkDTO(String id,
                          String name,
                          double mililiters,
                          boolean alcohol,
                          String photo,
                          int units,
                          String state) {
}
