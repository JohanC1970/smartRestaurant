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

    // Dueño del restaurante - Acceso completo a todas las funcionalidades del
    // restaurante
    RESTAURANTE("Dueño de Restaurante", 4),

    // Personal de cocina - Gestión de órdenes y menú
    COCINA("Cocina", 3),

    // Mesero - Gestión de mesas, órdenes y atención al cliente
    MESERO("Mesero", 2),

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
     * @return true si el rol es RESTAURANTE
     */
    public boolean isAdmin() {
        return this == RESTAURANTE;
    }

    /**
     * Verifica si este rol es personal del restaurante (no cliente)
     * 
     * @return true si el rol es RESTAURANTE, COCINA o MESERO
     */
    public boolean isStaff() {
        return this == RESTAURANTE || this == COCINA || this == MESERO;
    }

    /**
     * Verifica si este rol puede gestionar órdenes
     * 
     * @return true si el rol puede gestionar órdenes
     */
    public boolean canManageOrders() {
        return this == RESTAURANTE || this == COCINA || this == MESERO;
    }

    /**
     * Verifica si este rol puede gestionar el menú
     * 
     * @return true si el rol puede gestionar el menú
     */
    public boolean canManageMenu() {
        return this == RESTAURANTE || this == COCINA;
    }

    /**
     * Verifica si este rol puede gestionar usuarios
     * 
     * @return true si el rol puede gestionar usuarios
     */
    public boolean canManageUsers() {
        return this == RESTAURANTE;
    }
}
