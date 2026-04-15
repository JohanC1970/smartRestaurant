package com.smartRestaurant.orders.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Contrato del servicio de notificaciones en tiempo real (SSE).
 *
 * Define dos tipos de operaciones:
 *   - subscribe*: el cliente abre una conexión y queda escuchando
 *   - notify*   : el sistema dispara un evento a los suscritos
 */
public interface SseService {

    // ── Suscripción ────────────────────────────────────────────────────────

    /** Abre una conexión SSE para la vista de cocina. */
    SseEmitter subscribeKitchen();

    /** Abre una conexión SSE para la app del mesero. */
    SseEmitter subscribeWaiter();

    /** Abre una conexión SSE para un cliente específico (por su userId). */
    SseEmitter subscribeCustomer(Long customerId);

    // ── Notificaciones ─────────────────────────────────────────────────────

    /** Notifica a toda la cocina que llegó un pedido nuevo. */
    void notifyKitchen(Object orderData);

    /** Notifica a todos los meseros que un pedido está listo para recoger. */
    void notifyWaiterOrderReady(Object orderData);

    /** Notifica al cliente específico que su pedido está listo. */
    void notifyCustomerOrderReady(Long customerId, Object orderData);
}
