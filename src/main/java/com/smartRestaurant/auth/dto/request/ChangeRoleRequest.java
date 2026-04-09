package com.smartRestaurant.auth.dto.request;

import com.smartRestaurant.auth.model.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleRequest {

    @NotNull(message = "El rol es obligatorio")
    private UserRole role;

}
