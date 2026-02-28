package com.smartRestaurant.inventory.dto.Suplier;

import com.smartRestaurant.inventory.model.State;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CreateSuplierDTO(@NotBlank @Length(min = 1, max = 50)
                               String name,
                               @NotBlank @Length(min = 1, max = 50)
                               String address,
                               @NotBlank @Length(min = 1, max = 10)
                               String phone,
                               @NotBlank @Email @Length(min = 1, max = 50)
                               String email){
}
