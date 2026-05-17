package com.smartRestaurant.orders.model.enums;

public enum OrderStatus {

    /** Borrador: el mesero/cliente está construyendo el pedido, aún no enviado a cocina. */
    PENDING,

    /** Enviado: el pedido fue confirmado y cocina lo recibió. */
    SENT,

    /** En preparación: cocina inició el trabajo. */
    IN_PROGRESS,

    /** Listo: cocina terminó, esperando ser llevado a la mesa. */
    COMPLETED,

    /** Entregado: el cliente recibió su pedido. */
    DELIVERED,

    /** Cancelado: puede ocurrir desde PENDING o SENT (con reglas de negocio). */
    CANCELLED
}
