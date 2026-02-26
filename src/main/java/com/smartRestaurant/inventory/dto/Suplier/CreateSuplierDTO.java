package com.smartRestaurant.inventory.dto.Suplier;

import com.smartRestaurant.inventory.model.State;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;

public record CreateSuplierDTO(String name,
                               String address,
                               String phone,
                               String email,
                               State state) {
}
