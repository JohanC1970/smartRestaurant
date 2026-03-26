package com.smartRestaurant.chatbot.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ChatbotApiKeyInterceptor implements HandlerInterceptor {

    @Value("${chatbot.api.key}")
    private String expectedApiKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Buscamos el header personalizado
        String requestApiKey = request.getHeader("X-Chatbot-API-Key");

        // Si no trae la llave o es incorrecta, lo bloqueamos
        if (requestApiKey == null || !requestApiKey.equals(expectedApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Devuelve error 401
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Acceso denegado. API Key invalida o ausente.\"}");
            return false; // Detiene la petición aquí mismo
        }

        return true; // Si la llave es correcta, deja pasar la petición al Controlador
    }

}
