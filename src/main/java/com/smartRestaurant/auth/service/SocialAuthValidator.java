package com.smartRestaurant.auth.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.smartRestaurant.auth.dto.SocialUserInfo;
import com.smartRestaurant.auth.model.enums.SocialProvider;
import com.smartRestaurant.common.exception.AuthException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para validar tokens de proveedores sociales y obtener información de
 * usuarios
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SocialAuthValidator {

    private final RestTemplate restTemplate;

    @Value("${social.google.client-id:}")
    private String googleClientId;

    /**
     * Valida el token y obtiene información del usuario según el proveedor
     * 
     * @param provider    Proveedor social (GOOGLE, FACEBOOK, GITHUB)
     * @param accessToken Token de acceso del proveedor
     * @return SocialUserInfo con la información del usuario
     */
    public SocialUserInfo validateAndGetUserInfo(SocialProvider provider, String accessToken) {
        return switch (provider) {
            case GOOGLE -> validateGoogleToken(accessToken);
            case FACEBOOK -> validateFacebookToken(accessToken);
            case GITHUB -> validateGitHubToken(accessToken);
        };
    }

    /**
     * Valida token de Google y obtiene información del usuario
     */
    private SocialUserInfo validateGoogleToken(String accessToken) {
        try {
            // GSI brinda un ID Token, no un Access Token.
            // Se debe validar contra el endpoint tokeninfo.
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + accessToken;

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> userInfo = response.getBody();

                // Verificar que el token fue emitido para nuestro cliente (opcional pero
                // recomendado)
                String aud = (String) userInfo.get("aud");
                if (googleClientId != null && !googleClientId.isEmpty() && !googleClientId.equals(aud)) {
                    log.error("Token de Google aud mismatch: {} vs {}", googleClientId, aud);
                    // throw new AuthException("Token de Google no pertenece a esta aplicación");
                }

                return SocialUserInfo.builder()
                        .providerId((String) userInfo.get("sub"))
                        .email((String) userInfo.get("email"))
                        .firstName((String) userInfo.get("given_name"))
                        .lastName((String) userInfo.get("family_name"))
                        .profilePicture((String) userInfo.get("picture"))
                        .emailVerified(
                                Boolean.parseBoolean(String.valueOf(userInfo.getOrDefault("email_verified", "false"))))
                        .build();
            }

            throw new AuthException("Token de Google inválido");

        } catch (Exception e) {
            log.error("Error validando token de Google: {}", e.getMessage());
            throw new AuthException("Error al validar token de Google: " + e.getMessage());
        }
    }

    /**
     * Valida token de Facebook y obtiene información del usuario
     */
    @SuppressWarnings("unchecked")
    private SocialUserInfo validateFacebookToken(String accessToken) {
        try {
            String url = "https://graph.facebook.com/me?fields=id,email,first_name,last_name,picture&access_token="
                    + accessToken;

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> userInfo = response.getBody();
                Map<String, Object> picture = (Map<String, Object>) userInfo.get("picture");
                Map<String, Object> pictureData = picture != null ? (Map<String, Object>) picture.get("data") : null;

                return SocialUserInfo.builder()
                        .providerId((String) userInfo.get("id"))
                        .email((String) userInfo.get("email"))
                        .firstName((String) userInfo.get("first_name"))
                        .lastName((String) userInfo.get("last_name"))
                        .profilePicture(pictureData != null ? (String) pictureData.get("url") : null)
                        .emailVerified(true) // Facebook solo retorna emails verificados
                        .build();
            }

            throw new AuthException("Token de Facebook inválido");

        } catch (Exception e) {
            log.error("Error validando token de Facebook: {}", e.getMessage());
            throw new AuthException("Error al validar token de Facebook: " + e.getMessage());
        }
    }

    /**
     * Valida token de GitHub y obtiene información del usuario
     */
    private SocialUserInfo validateGitHubToken(String accessToken) {
        try {
            String url = "https://api.github.com/user";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.set("Accept", "application/vnd.github.v3+json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> userInfo = response.getBody();

                // GitHub puede no tener email público, obtenerlo de otro endpoint
                String email = (String) userInfo.get("email");
                if (email == null) {
                    email = getGitHubEmail(accessToken);
                }

                String name = (String) userInfo.get("name");
                String[] nameParts = name != null ? name.split(" ", 2) : new String[] { "", "" };

                return SocialUserInfo.builder()
                        .providerId(String.valueOf(userInfo.get("id")))
                        .email(email)
                        .firstName(nameParts.length > 0 ? nameParts[0] : "")
                        .lastName(nameParts.length > 1 ? nameParts[1] : "")
                        .profilePicture((String) userInfo.get("avatar_url"))
                        .emailVerified(true)
                        .build();
            }

            throw new AuthException("Token de GitHub inválido");

        } catch (Exception e) {
            log.error("Error validando token de GitHub: {}", e.getMessage());
            throw new AuthException("Error al validar token de GitHub: " + e.getMessage());
        }
    }

    /**
     * Obtiene el email principal de GitHub (puede ser privado)
     */
    private String getGitHubEmail(String accessToken) {
        try {
            String url = "https://api.github.com/user/emails";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.set("Accept", "application/vnd.github.v3+json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                for (Map<String, Object> emailInfo : response.getBody()) {
                    if ((Boolean) emailInfo.get("primary")) {
                        return (String) emailInfo.get("email");
                    }
                }
            }

            return null;

        } catch (Exception e) {
            log.error("Error obteniendo email de GitHub: {}", e.getMessage());
            return null;
        }
    }
}
