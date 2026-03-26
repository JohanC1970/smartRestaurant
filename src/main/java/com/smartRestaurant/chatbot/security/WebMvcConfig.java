package com.smartRestaurant.chatbot.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ChatbotApiKeyInterceptor apiKeyInterceptor;

    public WebMvcConfig(ChatbotApiKeyInterceptor apiKeyInterceptor) {
        this.apiKeyInterceptor = apiKeyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Aplica la seguridad EXCLUSIVAMENTE a las rutas que empiecen por /api/v1/chatbot/
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/api/chatbot/**");
    }
}
