package com.smartRestaurant.inventory.dto.Product;

import com.smartRestaurant.inventory.dto.Suplier.GetSuplierDTO;
import com.smartRestaurant.inventory.model.State;
import com.smartRestaurant.inventory.model.Suplier;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record GetProductDTO(String id,
                            String name,
                            String description,
                            double price,
                            double weight,
                            List<String> photos,
                            double minimumStock,
                            GetSuplierDTO suplier) {
}
