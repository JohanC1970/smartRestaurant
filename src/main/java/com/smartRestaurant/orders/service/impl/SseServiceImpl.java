package com.smartRestaurant.orders.service.impl;

import com.smartRestaurant.orders.dto.SseNotificationDTO;
import com.smartRestaurant.orders.service.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementación del servicio SSE.
 *
 * Guarda las conexiones abiertas y sabe cómo enviar eventos a cada canal.
 *
 * CopyOnWriteArrayList → lista thread-safe para broadcast (cocina, meseros)
 * ConcurrentHashMap    → mapa thread-safe para unicast por userId (clientes)
 */
@Service
@Slf4j
public class SseServiceImpl implements SseService {

    private static final long EMITTER_TIMEOUT = 30 * 60 * 1000L; // 30 minutos

    private final CopyOnWriteArrayList<SseEmitter> kitchenEmitters  = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<SseEmitter> waiterEmitters   = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<Long, SseEmitter> customerEmitters = new ConcurrentHashMap<>();

    // =====================================================================
    // SUSCRIPCIÓN
    // =====================================================================

    @Override
    public SseEmitter subscribeKitchen() {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        kitchenEmitters.add(emitter);
        log.info("[SSE] Cocina conectada. Total activos: {}", kitchenEmitters.size());

        emitter.onCompletion(() -> kitchenEmitters.remove(emitter));
        emitter.onTimeout(()    -> kitchenEmitters.remove(emitter));
        emitter.onError(e       -> kitchenEmitters.remove(emitter));

        return emitter;
    }

    @Override
    public SseEmitter subscribeWaiter() {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        waiterEmitters.add(emitter);
        log.info("[SSE] Mesero conectado. Total activos: {}", waiterEmitters.size());

        emitter.onCompletion(() -> waiterEmitters.remove(emitter));
        emitter.onTimeout(()    -> waiterEmitters.remove(emitter));
        emitter.onError(e       -> waiterEmitters.remove(emitter));

        return emitter;
    }

    @Override
    public SseEmitter subscribeCustomer(Long customerId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        customerEmitters.put(customerId, emitter);
        log.info("[SSE] Cliente {} conectado.", customerId);

        emitter.onCompletion(() -> customerEmitters.remove(customerId));
        emitter.onTimeout(()    -> customerEmitters.remove(customerId));
        emitter.onError(e       -> customerEmitters.remove(customerId));

        return emitter;
    }

    // =====================================================================
    // NOTIFICACIONES
    // =====================================================================

    @Override
    public void notifyKitchen(Object orderData) {
        broadcast(kitchenEmitters, new SseNotificationDTO("NEW_ORDER", "Nuevo pedido recibido", orderData));
    }

    @Override
    public void notifyWaiterOrderReady(Object orderData) {
        broadcast(waiterEmitters, new SseNotificationDTO("ORDER_READY", "Pedido listo para entregar", orderData));
    }

    @Override
    public void notifyCustomerOrderReady(Long customerId, Object orderData) {
        SseEmitter emitter = customerEmitters.get(customerId);
        if (emitter == null) {
            log.info("[SSE] Cliente {} no está conectado, se omite notificación.", customerId);
            return;
        }
        sendToEmitter(emitter,
            new SseNotificationDTO("YOUR_ORDER_READY", "Tu pedido está listo", orderData),
            () -> customerEmitters.remove(customerId));
    }

    // =====================================================================
    // PRIVADOS
    // =====================================================================

    private void broadcast(CopyOnWriteArrayList<SseEmitter> emitters, SseNotificationDTO notification) {
        log.info("[SSE] Enviando '{}' a {} cliente(s)", notification.type(), emitters.size());

        emitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                    .name(notification.type())
                    .data(notification));
                return false;
            } catch (IOException e) {
                log.warn("[SSE] Emitter inactivo eliminado: {}", e.getMessage());
                return true;
            }
        });
    }

    private void sendToEmitter(SseEmitter emitter, SseNotificationDTO notification, Runnable cleanup) {
        try {
            emitter.send(SseEmitter.event()
                .name(notification.type())
                .data(notification));
        } catch (IOException e) {
            log.warn("[SSE] Error enviando a cliente: {}", e.getMessage());
            cleanup.run();
        }
    }
}
