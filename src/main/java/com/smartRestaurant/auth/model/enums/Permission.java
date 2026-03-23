package com.smartRestaurant.auth.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum que define los permisos granulares del sistema.
 * Cada permiso representa una acción específica que un rol puede realizar.
 */
@Getter
@RequiredArgsConstructor
public enum Permission {

    // ═══════════════════════════════════════════════════════════════════════════
    // PERMISOS DE ADMINISTRACIÓN
    // ═══════════════════════════════════════════════════════════════════════════
    
    ADMIN_READ("admin:read", "Ver panel de administración"),
    ADMIN_WRITE("admin:write", "Modificar configuración de administración"),
    
    // Gestión de usuarios
    USER_READ("user:read", "Ver usuarios"),
    USER_WRITE("user:write", "Crear y editar usuarios"),
    USER_DELETE("user:delete", "Eliminar usuarios"),
    
    // Auditoría
    AUDIT_READ("audit:read", "Ver logs de auditoría"),
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PERMISOS DE INVENTARIO
    // ═══════════════════════════════════════════════════════════════════════════
    
    INVENTORY_READ("inventory:read", "Ver inventario"),
    INVENTORY_WRITE("inventory:write", "Crear y editar inventario"),
    INVENTORY_DELETE("inventory:delete", "Eliminar productos del inventario"),
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PERMISOS DE PROVEEDORES
    // ═══════════════════════════════════════════════════════════════════════════
    
    SUPPLIER_READ("supplier:read", "Ver proveedores"),
    SUPPLIER_WRITE("supplier:write", "Crear y editar proveedores"),
    SUPPLIER_DELETE("supplier:delete", "Eliminar proveedores"),
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PERMISOS DE PLATOS
    // ═══════════════════════════════════════════════════════════════════════════
    
    DISH_READ("dish:read", "Ver platos"),
    DISH_WRITE("dish:write", "Crear y editar platos"),
    DISH_DELETE("dish:delete", "Eliminar platos"),
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PERMISOS DE BEBIDAS
    // ═══════════════════════════════════════════════════════════════════════════
    
    DRINK_READ("drink:read", "Ver bebidas"),
    DRINK_WRITE("drink:write", "Crear y editar bebidas"),
    DRINK_DELETE("drink:delete", "Eliminar bebidas"),
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PERMISOS DE ADICIONES
    // ═══════════════════════════════════════════════════════════════════════════
    
    ADDITION_READ("addition:read", "Ver adiciones"),
    ADDITION_WRITE("addition:write", "Crear y editar adiciones"),
    ADDITION_DELETE("addition:delete", "Eliminar adiciones"),
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PERMISOS DE MENÚ DEL DÍA
    // ═══════════════════════════════════════════════════════════════════════════
    
    DAILY_MENU_READ("daily_menu:read", "Ver menú del día"),
    DAILY_MENU_WRITE("daily_menu:write", "Crear y editar menú del día"),
    DAILY_MENU_DELETE("daily_menu:delete", "Eliminar menú del día"),
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PERMISOS DE CATEGORÍAS
    // ═══════════════════════════════════════════════════════════════════════════
    
    CATEGORY_READ("category:read", "Ver categorías"),
    CATEGORY_WRITE("category:write", "Crear y editar categorías"),
    CATEGORY_DELETE("category:delete", "Eliminar categorías"),
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PERMISOS DE ALERTAS DE STOCK
    // ═══════════════════════════════════════════════════════════════════════════
    
    STOCK_ALERT_READ("stock_alert:read", "Ver alertas de stock"),
    
    // ═══════════════════════════════════════════════════════════════════════════
    // PERMISOS DE MOVIMIENTOS DE INVENTARIO
    // ═══════════════════════════════════════════════════════════════════════════
    
    INVENTORY_MOVEMENT_READ("inventory_movement:read", "Ver movimientos de inventario"),
    INVENTORY_MOVEMENT_WRITE("inventory_movement:write", "Registrar movimientos de inventario");

    private final String permission;
    private final String description;

    /**
     * Obtiene el nombre del permiso
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Verifica si el permiso es de lectura
     */
    public boolean isReadPermission() {
        return permission.endsWith(":read");
    }

    /**
     * Verifica si el permiso es de escritura
     */
    public boolean isWritePermission() {
        return permission.endsWith(":write");
    }

    /**
     * Verifica si el permiso es de eliminación
     */
    public boolean isDeletePermission() {
        return permission.endsWith(":delete");
    }
}
