package com.smartRestaurant.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de RestTemplate para realizar llamadas HTTP a APIs externas
 * Usado principalmente para validar tokens de proveedores sociales
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
