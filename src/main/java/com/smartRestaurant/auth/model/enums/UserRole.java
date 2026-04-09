package com.smartRestaurant.auth.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Enum que define los roles de usuario en el sistema de restaurante.
 * Cada rol tiene un nivel de privilegio asociado para control de acceso
 * jerárquico y un conjunto de permisos específicos.
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {

    // Administrador del sistema - Acceso completo a todas las funcionalidades
    ADMIN("Administrador", 4, Set.of(
        // Administración
        Permission.ADMIN_READ,
        Permission.ADMIN_WRITE,
        Permission.USER_READ,
        Permission.USER_WRITE,
        Permission.USER_DELETE,
        Permission.AUDIT_READ,
        // Inventario
        Permission.INVENTORY_READ,
        Permission.INVENTORY_WRITE,
        Permission.INVENTORY_DELETE,
        // Proveedores
        Permission.SUPPLIER_READ,
        Permission.SUPPLIER_WRITE,
        Permission.SUPPLIER_DELETE,
        // Platos
        Permission.DISH_READ,
        Permission.DISH_WRITE,
        Permission.DISH_DELETE,
        // Bebidas
        Permission.DRINK_READ,
        Permission.DRINK_WRITE,
        Permission.DRINK_DELETE,
        // Adiciones
        Permission.ADDITION_READ,
        Permission.ADDITION_WRITE,
        Permission.ADDITION_DELETE,
        // Menú del día
        Permission.DAILY_MENU_READ,
        Permission.DAILY_MENU_WRITE,
        Permission.DAILY_MENU_DELETE,
        // Categorías
        Permission.CATEGORY_READ,
        Permission.CATEGORY_WRITE,
        Permission.CATEGORY_DELETE,
        // Alertas de stock
        Permission.STOCK_ALERT_READ,
        // Movimientos de inventario
        Permission.INVENTORY_MOVEMENT_READ,
        Permission.INVENTORY_MOVEMENT_WRITE
    )),

    // Personal de cocina - Gestión de órdenes y menú
    KITCHEN("Cocina", 3, Set.of(
        // Inventario
        Permission.INVENTORY_READ,
        Permission.INVENTORY_WRITE,
        Permission.INVENTORY_DELETE,
        // Proveedores
        Permission.SUPPLIER_READ,
        Permission.SUPPLIER_WRITE,
        Permission.SUPPLIER_DELETE,
        // Platos
        Permission.DISH_READ,
        Permission.DISH_WRITE,
        Permission.DISH_DELETE,
        // Bebidas
        Permission.DRINK_READ,
        Permission.DRINK_WRITE,
        Permission.DRINK_DELETE,
        // Adiciones
        Permission.ADDITION_READ,
        Permission.ADDITION_WRITE,
        Permission.ADDITION_DELETE,
        // Menú del día
        Permission.DAILY_MENU_READ,
        Permission.DAILY_MENU_WRITE,
        Permission.DAILY_MENU_DELETE,
        // Categorías
        Permission.CATEGORY_READ,
        Permission.CATEGORY_WRITE,
        Permission.CATEGORY_DELETE,
        // Alertas de stock
        Permission.STOCK_ALERT_READ,
        // Movimientos de inventario
        Permission.INVENTORY_MOVEMENT_READ,
        Permission.INVENTORY_MOVEMENT_WRITE
    )),

    // Mesero - Solo lectura de menú y alertas
    WAITER("Mesero", 2, Set.of(
        // Solo lectura de platos, bebidas, adiciones y menú del día
        Permission.DISH_READ,
        Permission.DRINK_READ,
        Permission.ADDITION_READ,
        Permission.DAILY_MENU_READ,
        Permission.STOCK_ALERT_READ
    )),

    // Cliente - Sin acceso al dashboard administrativo
    CUSTOMER("Cliente", 1, Set.of());

    // Nombre legible para mostrar en la interfaz de usuario
    private final String displayName;

    /**
     * Nivel de privilegio del rol (1-4, siendo 4 el más alto)
     * Útil para implementar control de acceso jerárquico
     */
    private final int privilegeLevel;

    /**
     * Conjunto de permisos asociados a este rol
     */
    private final Set<Permission> permissions;

    /**
     * Obtiene las autoridades de Spring Security para este rol
     * Incluye el rol mismo y todos sus permisos
     */
    public Set<SimpleGrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());
        
        // Agregar el rol como autoridad (con prefijo ROLE_)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        
        return authorities;
    }

    /**
     * Verifica si este rol tiene un permiso específico
     */
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

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
