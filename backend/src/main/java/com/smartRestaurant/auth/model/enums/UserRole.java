package com.smartRestaurant.auth.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum que define los roles de usuario en el sistema de restaurante.
 * Cada rol tiene un nivel de privilegio asociado para control de acceso
 * jerárquico.
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {

    // Administrador del sistema - Acceso completo a todas las funcionalidades
    ADMIN("Administrador", 4),

    // Personal de cocina - Gestión de órdenes y menú
    KITCHEN("Cocina", 3),

    // Mesero - Gestión de mesas, órdenes y atención al cliente
    WAITER("Mesero", 2),

    // Cliente - Acceso limitado para realizar pedidos
    CUSTOMER("Cliente", 1);

    // Nombre legible para mostrar en la interfaz de usuario
    private final String displayName;

    /**
     * Nivel de privilegio del rol (1-4, siendo 4 el más alto)
     * Útil para implementar control de acceso jerárquico
     */
    private final int privilegeLevel;

    /**
     * Verifica si este rol tiene al menos el nivel de privilegio especificado
     * 
     * @param level Nivel mínimo requerido
     * @return true si el rol tiene suficiente privilegio
     */
    public boolean hasPrivilegeLevel(int level) {
        return this.privilegeLevel >= level;
    }

    /**
     * Verifica si este rol es administrador
     * 
     * @return true si el rol es ADMIN
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Verifica si este rol es personal del restaurante (no cliente)
     * 
     * @return true si el rol es ADMIN, KITCHEN o WAITER
     */
    public boolean isStaff() {
        return this == ADMIN || this == KITCHEN || this == WAITER;
    }

    /**
     * Verifica si este rol puede gestionar órdenes
     * 
     * @return true si el rol puede gestionar órdenes
     */
    public boolean canManageOrders() {
        return this == ADMIN || this == KITCHEN || this == WAITER;
    }

    /**
     * Verifica si este rol puede gestionar el menú
     * 
     * @return true si el rol puede gestionar el menú
     */
    public boolean canManageMenu() {
        return this == ADMIN || this == KITCHEN;
    }

    /**
     * Verifica si este rol puede gestionar usuarios
     * 
     * @return true si el rol puede gestionar usuarios
     */
    public boolean canManageUsers() {
        return this == ADMIN;
    }
}
