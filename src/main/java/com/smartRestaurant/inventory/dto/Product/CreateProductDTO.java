package com.smartRestaurant.inventory.dto.Product;

import com.smartRestaurant.inventory.model.State;
import com.smartRestaurant.inventory.model.Suplier;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record CreateProductDTO( @NotBlank @Length(min = 1, max = 50)
                                String name,
                                @NotBlank @Length(min = 10, max = 500)
                                String description,
                                @Positive @NotNull
                                double price,
                                @Positive @NotNull
                                double weight,
                                @NotNull @Size(min = 1, max = 10)
                                List<String> photos,
                                @Positive @NotNull
                                double minimumStock) {
}
