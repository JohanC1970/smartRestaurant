package com.smartRestaurant.inventory.model;

public enum DishAvailability {
    /** Visible en la carta normal del cliente */
    REGULAR,
    /** Reservado exclusivamente para el Menú del Día; invisible en la carta normal */
    MENU_DEL_DIA,
    /** Aparece en la carta normal Y puede asignarse al Menú del Día */
    BOTH
}
