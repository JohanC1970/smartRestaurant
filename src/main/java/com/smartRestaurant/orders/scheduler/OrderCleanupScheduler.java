package com.smartRestaurant.orders.scheduler;

import com.smartRestaurant.orders.model.Order;
import com.smartRestaurant.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Limpieza automática de órdenes online abandonadas en la pasarela de pago.
 *
 * Si un cliente crea una orden, entra a Wompi y cierra el navegador (sin
 * que el frontend llegue a llamar al endpoint /abandon), la orden quedaría
 * huérfana en la BD para siempre.  Este scheduler la elimina después de
 * EXPIRATION_MINUTES minutos sin que el pago se haya confirmado.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCleanupScheduler {

    /** Tiempo máximo que una orden PENDING online puede estar sin pago (minutos). */
    private static final int EXPIRATION_MINUTES = 30;

    private final OrderRepository orderRepository;

    /**
     * Se ejecuta cada 15 minutos.
     * fixedDelay: 15 min = 900_000 ms
     */
    @Scheduled(fixedDelay = 900_000)
    @Transactional
    public void cleanupAbandonedOrders() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(EXPIRATION_MINUTES);

        List<Order> abandoned = orderRepository.findAbandonedOnlineOrders(expirationTime);

        if (abandoned.isEmpty()) {
            return;
        }

        log.info("[CLEANUP] Eliminando {} orden(es) online abandonada(s) (sin pago por más de {} min)",
                abandoned.size(), EXPIRATION_MINUTES);

        for (Order order : abandoned) {
            log.info("[CLEANUP] Eliminando orden abandonada: id={}, creada={}", order.getId(), order.getCreatedAt());
            orderRepository.deleteById(order.getId());
        }

        log.info("[CLEANUP] Limpieza completada. {} orden(es) eliminada(s).", abandoned.size());
    }
}
