# Protección de Controladores - RBAC

## Resumen de Implementación

Este documento describe cómo proteger cada controlador de inventory con las anotaciones de seguridad apropiadas según los roles y permisos definidos.

## Roles y Permisos

### ADMIN (Nivel 4)
- Acceso completo a todas las funcionalidades
- Permisos: READ, WRITE, DELETE en todos los módulos

### KITCHEN (Nivel 3)
- Acceso completo a: Inventario, Proveedores, Platos, Bebidas, Adiciones, Menú del Día, Categorías, Alertas de Stock, Movimientos
- Permisos: READ, WRITE, DELETE

### WAITER (Nivel 2)
- Acceso de SOLO LECTURA a: Platos, Bebidas, Adiciones, Menú del Día, Alertas de Stock
- Permisos: READ únicamente

### CUSTOMER (Nivel 1)
- Sin acceso al dashboard administrativo

## Anotaciones por Controlador

### 1. DishController (api/dishes)
```java
// GET - Lectura
@PreAuthorize("hasAnyAuthority('dish:read', 'ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_WAITER')")

// POST, PUT - Escritura
@PreAuthorize("hasAnyAuthority('dish:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")

// DELETE - Eliminación
@PreAuthorize("hasAnyAuthority('dish:delete', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
```

### 2. DrinkController (api/drinks)
```java
// GET - Lectura
@PreAuthorize("hasAnyAuthority('drink:read', 'ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_WAITER')")

// POST, PUT - Escritura
@PreAuthorize("hasAnyAuthority('drink:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")

// DELETE - Eliminación
@PreAuthorize("hasAnyAuthority('drink:delete', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
```

### 3. AdditionController (api/additions)
```java
// GET - Lectura
@PreAuthorize("hasAnyAuthority('addition:read', 'ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_WAITER')")

// POST, PUT - Escritura
@PreAuthorize("hasAnyAuthority('addition:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")

// DELETE - Eliminación
@PreAuthorize("hasAnyAuthority('addition:delete', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
```

### 4. DailyMenuController (api/daily-menu)
```java
// GET - Lectura
@PreAuthorize("hasAnyAuthority('daily_menu:read', 'ROLE_ADMIN', 'ROLE_KITCHEN', 'ROLE_WAITER')")

// POST, PUT - Escritura
@PreAuthorize("hasAnyAuthority('daily_menu:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")

// DELETE - Eliminación
@PreAuthorize("hasAnyAuthority('daily_menu:delete', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
```

### 5. CategoryController (api/categories)
```java
// GET - Lectura
@PreAuthorize("hasAnyAuthority('category:read', 'ROLE_ADMIN', 'ROLE_KITCHEN')")

// POST, PUT - Escritura
@PreAuthorize("hasAnyAuthority('category:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")

// DELETE - Eliminación
@PreAuthorize("hasAnyAuthority('category:delete', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
```

### 6. ProductController (api/products - Inventario)
```java
// GET - Lectura
@PreAuthorize("hasAnyAuthority('inventory:read', 'ROLE_ADMIN', 'ROLE_KITCHEN')")

// POST, PUT - Escritura
@PreAuthorize("hasAnyAuthority('inventory:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")

// DELETE - Eliminación
@PreAuthorize("hasAnyAuthority('inventory:delete', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
```

### 7. SuplierController (api/suppliers)
```java
// GET - Lectura
@PreAuthorize("hasAnyAuthority('supplier:read', 'ROLE_ADMIN', 'ROLE_KITCHEN')")

// POST, PUT - Escritura
@PreAuthorize("hasAnyAuthority('supplier:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")

// DELETE - Eliminación
@PreAuthorize("hasAnyAuthority('supplier:delete', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
```

### 8. InventoryMovementController (api/inventory-movements)
```java
// GET - Lectura
@PreAuthorize("hasAnyAuthority('inventory_movement:read', 'ROLE_ADMIN', 'ROLE_KITCHEN')")

// POST, PUT - Escritura
@PreAuthorize("hasAnyAuthority('inventory_movement:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
```

### 9. ImageController (api/images)
```java
// Todos los métodos - Solo ADMIN y KITCHEN
@PreAuthorize("hasAnyRole('ADMIN', 'KITCHEN')")
```

## Pasos de Implementación

1. ✅ Crear enum Permission con todos los permisos
2. ✅ Actualizar UserRole para incluir permisos
3. ✅ Actualizar CustomUserDetailsService para incluir permisos en authorities
4. ✅ Habilitar @EnableMethodSecurity en SecurityConfig
5. ✅ Proteger DishController
6. ⏳ Proteger DrinkController
7. ⏳ Proteger AdditionController
8. ⏳ Proteger DailyMenuController
9. ⏳ Proteger CategoryController
10. ⏳ Proteger ProductController
11. ⏳ Proteger SuplierController
12. ⏳ Proteger InventoryMovementController
13. ⏳ Proteger ImageController

## Notas Importantes

- Los controladores de Admin (AdminController, AuditController) ya están protegidos con `@PreAuthorize("hasRole('ADMIN')")`
- Los endpoints de autenticación (AuthenticationController) son públicos
- WAITER solo tiene permisos de lectura en: Platos, Bebidas, Adiciones, Menú del Día, Alertas de Stock
- CUSTOMER no tiene acceso a ningún endpoint de inventory

## Testing

Después de implementar, probar:

1. ADMIN puede hacer todo
2. KITCHEN puede hacer CRUD en sus módulos
3. WAITER solo puede leer (GET) en sus módulos permitidos
4. WAITER recibe 403 al intentar POST/PUT/DELETE
5. CUSTOMER recibe 403 en todos los endpoints de inventory
