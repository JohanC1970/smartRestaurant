package com.smartRestaurant.orders.dto;

/**
 * DTO que representa un evento SSE enviado al frontend.
 *
 * @param type    identifica qué tipo de evento es (ej. "NEW_ORDER", "ORDER_READY")
 * @param message texto legible para mostrar al usuario
 * @param data    payload con los datos del evento (puede ser una orden, un ID, etc.)
 */
public record SseNotificationDTO(
    String type,
    String message,
    Object data
) {}
