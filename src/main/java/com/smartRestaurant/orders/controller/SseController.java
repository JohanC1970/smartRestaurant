package com.smartRestaurant.orders.controller;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.inventory.exceptions.BadRequestException;
import com.smartRestaurant.inventory.util.CurrentUserProvider;
import com.smartRestaurant.orders.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Endpoints de suscripción SSE.
 *
 * El frontend llama a uno de estos endpoints UNA SOLA VEZ al abrir la vista.
 * El servidor mantiene la conexión abierta y envía eventos cuando ocurren cosas.
 *
 * produces = TEXT_EVENT_STREAM_VALUE  ← le dice al browser que es un stream SSE
 */
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;
    private final CurrentUserProvider currentUserProvider;

    /**
     * GET /api/sse/kitchen
     * La vista de cocina se suscribe aquí.
     * Recibirá eventos tipo "NEW_ORDER" cada vez que llegue un pedido.
     */
    @GetMapping(value = "/kitchen", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_KITCHEN', 'ROLE_ADMIN')")
    public SseEmitter kitchenStream() {
        return sseService.subscribeKitchen();
    }

    /**
     * GET /api/sse/waiter
     * La app del mesero se suscribe aquí.
     * Recibirá eventos tipo "ORDER_READY" cuando la cocina termina un pedido.
     */
    @GetMapping(value = "/waiter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_WAITER', 'ROLE_ADMIN')")
    public SseEmitter waiterStream() {
        return sseService.subscribeWaiter();
    }

    /**
     * GET /api/sse/me
     * El cliente online se suscribe aquí tras hacer su pedido.
     * Recibirá el evento "YOUR_ORDER_READY" cuando su pedido esté listo.
     */
    @GetMapping(value = "/me", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public SseEmitter customerStream() {
        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser == null) {
            throw new BadRequestException("No hay usuario autenticado");
        }
        return sseService.subscribeCustomer(currentUser.getId());
    }
}
