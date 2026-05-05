package com.smartRestaurant.inventory.model;

public enum MenuStatus {
    DRAFT,      // Borrador: en configuración, no visible para pedidos
    PUBLISHED,  // Publicado: visible y disponible para pedidos
    SOLD_OUT,   // Agotado: sin porciones disponibles o sección obligatoria agotada
    CLOSED      // Cerrado: fin de franja horaria o cierre manual
}
