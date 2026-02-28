package com.smartRestaurant.inventory.dto.Suplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record GetSuplierDTO(String id,
                            String name,
                            String address,
                            String phone,
                            String email) {
}
