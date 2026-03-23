package com.smartRestaurant.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO interno que contiene la información del usuario obtenida del proveedor social
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserInfo {
    private String providerId;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private boolean emailVerified;
}
