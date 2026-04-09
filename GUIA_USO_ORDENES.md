# 🎯 GUÍA DE USO - MÓDULO DE ÓRDENES

## ✅ COMPILACIÓN EXITOSA

El proyecto ha compilado sin errores. Los warnings que aparecen son normales y corresponden a propiedades de MapStruct que se asignan manualmente.

---

## 📝 JSON CORRECTO PARA CREAR ORDEN

**Importante:** Los IDs de usuario (`customerId`, `waiterId`) son de tipo **Long**, no String.

```json
POST /api/orders
Content-Type: application/json

{
  "channel": "ONLINE",
  "customerId": 1,
  "waiterId": null,
  "tableNumber": null,
  "items": [
    {
      "productId": "DISH-001",
      "productType": "DISH",
      "quantity": 2,
      "notes": "sin picante"
    },
    {
      "productId": "DRINK-005",
      "productType": "DRINK",
      "quantity": 1,
      "notes": "con hielo"
    },
    {
      "productId": "ADD-003",
      "productType": "ADDITION",
      "quantity": 3,
      "notes": null
    }
  ]
}
```

---

## 📊 TIPOS DE DATOS

| Campo | Tipo | Obligatorio | Ejemplo |
|-------|------|------------|---------|
| `channel` | Enum | ✅ | `"ONLINE"` o `"PRESENCIAL"` |
| `customerId` | Long | ❌ | `1`, `2`, `3` (null si presencial) |
| `waiterId` | Long | ❌ | `5`, `10` (null si online) |
| `tableNumber` | String | ❌ | `"A1"`, `"3"` (null si online) |
| `items[].productId` | String | ✅ | `"DISH-001"` |
| `items[].productType` | String | ✅ | `"DISH"`, `"DRINK"`, `"ADDITION"` |
| `items[].quantity` | int | ✅ | `1`, `2`, `3` |
| `items[].notes` | String | ❌ | `"sin picante"` |

---

## 🌐 ENDPOINTS

### 1. Crear Orden
```bash
POST /api/orders
Authorization: Bearer {token}
Content-Type: application/json

Body: {ver JSON arriba}

Response (201 CREATED):
{
  "data": "ORD-550e8400-e29b-41d4-a716-446655440000",
  "hasError": false
}
```

### 2. Obtener Todas las Órdenes (Paginada)
```bash
GET /api/orders/{page}/page
Authorization: Bearer {token}

Response (200 OK):
{
  "data": [
    {
      "id": "ORD-550e8400-e29b-41d4-a716-446655440000",
      "status": "PENDING",
      "channel": "ONLINE",
      "customerName": "Juan Pérez",
      "createdAt": "2026-03-25T20:55:00",
      "itemCount": 3,
      "totalAmount": 125.50
    }
  ],
  "hasError": false
}
```

### 3. Obtener Detalle de Orden
```bash
GET /api/orders/{orderId}
Authorization: Bearer {token}

Response (200 OK):
{
  "data": {
    "id": "ORD-550e8400-e29b-41d4-a716-446655440000",
    "status": "PENDING",
    "channel": "ONLINE",
    "customerName": "Juan Pérez",
    "waiterId": null,
    "tableNumber": null,
    "createdAt": "2026-03-25T20:55:00",
    "updatedAt": "2026-03-25T20:55:00",
    "items": [
      {
        "id": "ITEM-001",
        "productId": "DISH-001",
        "productName": "Pasta Carbonara",
        "productType": "DISH",
        "quantity": 2,
        "unitPrice": 12.50,
        "totalPrice": 25.00,
        "notes": "sin picante"
      },
      {
        "id": "ITEM-002",
        "productId": "DRINK-005",
        "productName": "Coca Cola",
        "productType": "DRINK",
        "quantity": 1,
        "unitPrice": 2.50,
        "totalPrice": 2.50,
        "notes": "con hielo"
      },
      {
        "id": "ITEM-003",
        "productId": "ADD-003",
        "productName": "Queso Extra",
        "productType": "ADDITION",
        "quantity": 3,
        "unitPrice": 1.50,
        "totalPrice": 4.50,
        "notes": null
      }
    ],
    "totalAmount": 32.00,
    "paymentStatus": "PENDING"
  },
  "hasError": false
}
```

### 4. Actualizar Orden
```bash
PUT /api/orders/{orderId}
Authorization: Bearer {token}
Content-Type: application/json

Body:
{
  "status": "IN_PROGRESS",
  "tableNumber": "A1",
  "notes": null
}

Response (200 OK):
{
  "data": "Orden actualizada",
  "hasError": false
}
```

### 5. Cancelar Orden
```bash
PATCH /api/orders/{orderId}/cancel
Authorization: Bearer {token}

Response (200 OK):
{
  "data": "Orden cancelada",
  "hasError": false
}
```

### 6. Eliminar Orden
```bash
DELETE /api/orders/{orderId}
Authorization: Bearer {token}

Response (200 OK):
{
  "data": "Orden eliminada",
  "hasError": false
}
```

---

## 🔒 SEGURIDAD Y PERMISOS

| Endpoint | Permiso | Roles |
|----------|---------|-------|
| POST /api/orders | order:write | ADMIN, WAITER, CUSTOMER |
| GET /api/orders/{page}/page | order:read | ADMIN, WAITER, KITCHEN |
| GET /api/orders/{id} | order:read | ADMIN, WAITER, KITCHEN |
| PUT /api/orders/{id} | order:write | ADMIN, WAITER, KITCHEN |
| PATCH /api/orders/{id}/cancel | order:write | ADMIN, WAITER |
| DELETE /api/orders/{id} | order:delete | ADMIN |

---

## ⚠️ ERRORES COMUNES

### Error: "Cliente no encontrado"
**Causa:** El `customerId` no existe en la BD o es 0
**Solución:** Verifica que el ID del usuario sea correcto y exista

### Error: "Dish no encontrado"
**Causa:** El producto con ese ID no existe o está inactivo
**Solución:** Verifica que el producto exista y esté ACTIVE

### Error: "El tipo de producto no válido"
**Causa:** El `productType` no es "DISH", "DRINK" o "ADDITION"
**Solución:** Usa exactamente uno de esos valores

### Error: "La orden debe tener al menos un item"
**Causa:** La lista `items` está vacía o null
**Solución:** Agrega al menos un item

---

## 📚 ENUMERACIONES

### OrderStatus
```java
PENDING      // Pendiente
IN_PROGRESS  // En progreso
COMPLETED    // Completada
DELIVERED    // Entregada
CANCELLED    // Cancelada
```

### OrderChannel
```java
ONLINE      // Pedido online
PRESENCIAL  // Pedido presencial
```

### ProductType (en OrderItemDTO)
```java
DISH        // Plato
DRINK       // Bebida
ADDITION    // Adición/Extra
```

---

## 🧪 PRUEBAS CON CURL

```bash
# Crear orden
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "ONLINE",
    "customerId": 1,
    "items": [
      {"productId": "DISH-001", "productType": "DISH", "quantity": 1, "notes": null}
    ]
  }'

# Obtener órdenes (página 0)
curl -X GET http://localhost:8080/api/orders/0/page \
  -H "Authorization: Bearer {token}"

# Obtener detalle
curl -X GET http://localhost:8080/api/orders/ORD-xxxxx \
  -H "Authorization: Bearer {token}"

# Actualizar
curl -X PUT http://localhost:8080/api/orders/ORD-xxxxx \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"status": "IN_PROGRESS"}'

# Cancelar
curl -X PATCH http://localhost:8080/api/orders/ORD-xxxxx/cancel \
  -H "Authorization: Bearer {token}"

# Eliminar
curl -X DELETE http://localhost:8080/api/orders/ORD-xxxxx \
  -H "Authorization: Bearer {token}"
```

---

## 📊 FLUJO DE ESTADÍSTICAS

```
Crear Orden (POST)
    ↓
    └─ Status: PENDING
    
Actualizar a IN_PROGRESS (PUT)
    ↓
    └─ Mesero está preparando
    
Actualizar a COMPLETED (PUT)
    ↓
    └─ Orden lista
    
Actualizar a DELIVERED (PUT)
    ↓
    └─ Orden entregada ✓
    
Alternativamente:
    ├─ CANCELLED (PATCH) - Orden cancelada ✗
    └─ DELETE - Orden eliminada permanentemente
```

---

## 🎯 NOTAS IMPORTANTES

✅ `customerId` y `waiterId` son **Long**, no String  
✅ `productId` sigue siendo **String** (ej: "DISH-001")  
✅ Los IDs de orden generados son **UUID** en formato String  
✅ La paginación comienza en **página 0** (10 items por página)  
✅ Todas las respuestas siguen el formato `ResponseDTO<T>`  
✅ Las validaciones se hacen en el DTO y el servicio  
✅ Las órdenes tienen **cascada de eliminación** (se eliminan los items al eliminar la orden)  

---

## 🚀 LISTO PARA PRODUCCIÓN

El módulo está **compilado, testeado y listo para usar** en producción.


