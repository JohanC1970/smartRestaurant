package com.smartRestaurant.auth.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum que define los proveedores de autenticación social soportados
 */
@Getter
@RequiredArgsConstructor
public enum SocialProvider {

    GOOGLE("Google", "https://www.googleapis.com/oauth2/v3/userinfo"),
    FACEBOOK("Facebook", "https://graph.facebook.com/me"),
    GITHUB("GitHub", "https://api.github.com/user");

    private final String displayName;
    private final String apiEndpoint;
}
