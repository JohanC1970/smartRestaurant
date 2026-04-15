package com.smartRestaurant.orders.model.enums;

/**
 * Tipos de métodos de pago disponibles
 */
public enum PaymentMethodType {
    // ONLINE (Pasarelas digitales)
    WOMPI,           // Pasarela Wompi (tarjeta, transferencia, QR)
    
    // PRESENCIAL (Métodos locales)
    CASH,            // Efectivo
    CARD,            // Tarjeta física (POS/Datafono)
    TRANSFER,        // Transferencia bancaria
    CHECK            // Cheque
}

