# 📋 FLUJO COMPLETO DE CREACIÓN DE ÓRDENES

## 1️⃣ JSON DE ENTRADA (Cliente)

```json
POST /api/orders
{
  "channel": "ONLINE",
  "customerId": "USER-123",
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

## 2️⃣ CAPAS DE PROCESAMIENTO

### 🌐 **Controller Layer** (OrderController)
```
1. Recibe CreateOrderDto (record)
2. Valida anotaciones @Valid
3. Verifica permisos con @PreAuthorize
4. Llama a orderService.create()
5. Retorna ResponseDTO<orderId>
```

**HTTP Response 201 Created:**
```json
{
  "data": "ORD-uuid-12345",
  "hasError": false
}
```

---

### 🔧 **Service Layer** (OrderServiceImpl)

#### PASO 1: Validar Datos
```java
✓ Channel no null
✓ Items lista no vacía
✓ productId obligatorio
✓ productType válido (DISH, DRINK, ADDITION)
```

#### PASO 2: Cargar Usuarios (si aplica)
```java
// Si es ONLINE
User customer = userRepository.findById(customerId)
  ↓ Exception: "Cliente no encontrado"

// Si es PRESENCIAL
User waiter = userRepository.findById(waiterId)
  ↓ Exception: "Mesero no encontrado"
```

#### PASO 3: Crear Orden Base (usando Mapper)
```java
Order order = orderMapper.toEntity(createOrderDto)
  
Mapper realiza:
├─ id = UUID.randomUUID()
├─ status = PENDING
├─ createdAt = LocalDateTime.now()
└─ updatedAt = LocalDateTime.now()

order.setCustomer(customer)
order.setWaiter(waiter)
order.setTableNumber(tableNumber)
```

#### PASO 4: Procesar Cada Item
Para cada `CreateOrderItemDTO`:

```java
createOrderItem(itemDto, order)
  ├─ itemId = UUID.randomUUID()
  ├─ loadProductByType()
  │   ├─ Si "DISH" → dishRepository.findById()
  │   ├─ Si "DRINK" → drinkRepository.findById()
  │   └─ Si "ADDITION" → additionRepository.findById()
  ├─ Validar producto no INACTIVE
  ├─ orderItem.setProducto(producto)
  ├─ orderItem.setNotes(notes)
  └─ orderItemRepository.save()
```

#### PASO 5: Guardar Orden Completa
```java
order.setItems(items)
Order savedOrder = orderRepository.save(order)
  
// @Transactional asegura atomicidad:
// Si cualquier paso falla → rollback de todo
```

#### PASO 6: Retornar ID
```java
return savedOrder.getId()  // "ORD-uuid-12345"
```

---

### 📊 **Mapper Layer** (OrderMapper - MapStruct)

**Transformaciones automáticas:**

```java
Order toEntity(CreateOrderDto createOrderDto)
  ├─ id → UUID
  ├─ status → PENDING
  ├─ createdAt → now()
  ├─ channel → del DTO
  └─ customer/waiter → null (se asignan después)

GetOrdersDTO toListDTO(Order order)
  ├─ customerName ← order.customer.name
  ├─ itemCount ← order.items.size()
  ├─ totalAmount ← calculateTotalAmount(order)
  └─ paymentStatus ← order.payment.status

GetOrderDetailDTO toDetailDTO(Order order)
  ├─ items → List<GetOrderItemDTO>
  ├─ totalAmount → calculado
  └─ (ídem GetOrdersDTO + detalles adicionales)

GetOrderItemDTO toItemDTO(OrderItem<?> item)
  ├─ productName ← getProductName(item.producto)
  ├─ productType ← getProductType(item.producto)
  ├─ unitPrice ← getProductPrice(item.producto)
  └─ totalPrice ← idem
```

---

### 💾 **Repository Layer**

```
┌─────────────────────────────────────┐
│      OrderRepository                │
├─────────────────────────────────────┤
│ - save(Order)                       │
│ - findById(id)                      │
│ - findAll(Pageable)                 │
│ - deleteById(id)                    │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│    OrderItemRepository              │
├─────────────────────────────────────┤
│ - save(OrderItem)                   │
│ - findById(id)                      │
│ - deleteById(id)                    │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ Product Repositories                │
├─────────────────────────────────────┤
│ - DishRepository.findById()         │
│ - DrinkRepository.findById()        │
│ - AdditionRepository.findById()     │
└─────────────────────────────────────┘
```

---

## 3️⃣ DIAGRAMA DE FLUJO COMPLETO

```
┌────────────────────────────────────────────────────────────────────┐
│                         CLIENT (JSON)                              │
│  POST /api/orders                                                   │
└────────────────┬─────────────────────────────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────────────────────────────┐
│                    OrderController                                  │
│  • @Valid CreateOrderDto                                            │
│  • @PreAuthorize                                                    │
│  • Llama orderService.create()                                      │
└────────────────┬─────────────────────────────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────────────────────────────┐
│             OrderServiceImpl.create()                                │
│                                                                     │
│  1️⃣ validateCreateOrderDto()                                        │
│     └─ Verifica channel, items, productId, productType             │
│                                                                     │
│  2️⃣ userRepository.findById(customerId/waiterId)                    │
│     └─ Carga User si existe                                        │
│                                                                     │
│  3️⃣ orderMapper.toEntity(createOrderDto)                            │
│     ├─ id = UUID                                                    │
│     ├─ status = PENDING                                             │
│     ├─ createdAt = now()                                            │
│     └─ channel = del DTO                                            │
│                                                                     │
│  4️⃣ Para cada CreateOrderItemDTO:                                   │
│     └─ createOrderItem()                                            │
│        ├─ loadProductByType()                                      │
│        │  ├─ DISH → dishRepository.findById()                      │
│        │  ├─ DRINK → drinkRepository.findById()                    │
│        │  └─ ADDITION → additionRepository.findById()              │
│        ├─ Crear OrderItem<?>                                       │
│        ├─ setProducto(producto)                                    │
│        └─ orderItemRepository.save()                               │
│                                                                     │
│  5️⃣ order.setItems(items)                                           │
│     orderRepository.save(order)                                    │
│                                                                     │
│  6️⃣ return orderId                                                  │
│                                                                     │
│  🔄 @Transactional: Si falla algo → ROLLBACK TODO                  │
└────────────────┬─────────────────────────────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────────────────────────────┐
│                    DATABASE OPERATIONS                              │
│                                                                     │
│  📊 order TABLE:                                                    │
│  INSERT INTO order (id, status, channel, customer_id, waiter_id,   │
│                     table_number, created_at, updated_at)          │
│  VALUES ('ORD-12345', 'PENDING', 'ONLINE', 'CUST-001', NULL,      │
│          NULL, now(), now())                                       │
│                                                                     │
│  📊 order_item TABLE:                                               │
│  INSERT INTO order_item (id, order_id, product_type, product_id,   │
│                          notes) VALUES                             │
│    ('ITEM-001', 'ORD-12345', 'DISH', 'DISH-001', 'sin picante')   │
│    ('ITEM-002', 'ORD-12345', 'DRINK', 'DRINK-005', 'con hielo')   │
│    ('ITEM-003', 'ORD-12345', 'ADDITION', 'ADD-003', NULL)         │
│                                                                     │
└────────────────┬─────────────────────────────────────────────────┘
                 │
                 ▼
┌────────────────────────────────────────────────────────────────────┐
│                  RESPONSE (HTTP 201 CREATED)                        │
│                                                                     │
│  {                                                                  │
│    "data": "ORD-12345",                                             │
│    "hasError": false                                                │
│  }                                                                  │
│                                                                     │
│  El cliente recibe el ID para futuras operaciones                  │
└────────────────────────────────────────────────────────────────────┘
```

---

## 4️⃣ OTROS ENDPOINTS Y SU FLUJO

### 📖 GET /api/orders/{page}/page
```
1. OrderController.getAllOrders(page)
2. OrderServiceImpl.getAll(page)
   ├─ PageRequest.of(page, 10)
   ├─ orderRepository.findAll(pageable)
   └─ orders.stream().map(orderMapper::toListDTO)
3. OrderMapper.toListDTO()
   ├─ customerName
   ├─ itemCount
   ├─ totalAmount (calculado)
   └─ paymentStatus
4. ResponseDTO<List<GetOrdersDTO>>
```

### 📖 GET /api/orders/{id}
```
1. OrderController.getOrderById(id)
2. OrderServiceImpl.getById(id)
   ├─ orderRepository.findById(id)
   └─ orderMapper.toDetailDTO(order)
3. OrderMapper.toDetailDTO()
   ├─ items → List<GetOrderItemDTO>
   ├─ customerName
   ├─ totalAmount
   └─ paymentStatus
4. ResponseDTO<GetOrderDetailDTO>
```

### ✏️ PUT /api/orders/{id}
```
1. OrderController.updateOrder(id, UpdateOrderDTO)
2. OrderServiceImpl.update(id, updateOrderDTO)
   ├─ orderRepository.findById(id)
   ├─ orderMapper.updateOrder(dto, order)
   │  └─ @Mapping para status
   ├─ LocalDateTime.now() → updatedAt
   └─ orderRepository.save(order)
3. ResponseDTO<"Orden actualizada">
```

### ❌ PATCH /api/orders/{id}/cancel
```
1. OrderController.cancelOrder(id)
2. OrderServiceImpl.cancel(id)
   ├─ Validar que NO esté COMPLETED o DELIVERED
   ├─ status = CANCELLED
   ├─ updatedAt = now()
   └─ orderRepository.save(order)
3. ResponseDTO<"Orden cancelada">
```

### 🗑️ DELETE /api/orders/{id}
```
1. OrderController.deleteOrder(id)
2. OrderServiceImpl.delete(id)
   ├─ orderRepository.existsById(id)
   └─ orderRepository.deleteById(id)
3. ResponseDTO<"Orden eliminada">
```

---

## 5️⃣ VALIDACIONES Y EXCEPCIONES

### ✅ Validaciones en CreateOrderDto (anotaciones):
- `@NotNull` channel
- `@NotEmpty` items
- `@Valid` en items (valida cada CreateOrderItemDTO)

### ✅ Validaciones en CreateOrderItemDTO:
- `@NotBlank` productId
- `@NotBlank` productType
- `@Positive` quantity

### ❌ Excepciones Personalizadas:
```java
OrderNotFoundException
  ├─ "Orden no encontrada: {id}"
  └─ Se lanza en getById, update, cancel, delete

RuntimeException
  ├─ "El canal de la orden es obligatorio"
  ├─ "La orden debe tener al menos un item"
  ├─ "Cliente no encontrado"
  ├─ "Mesero no encontrado"
  ├─ "Dish no encontrado o inactivo"
  ├─ "Drink no encontrado o inactivo"
  ├─ "Addition no encontrada o inactiva"
  ├─ "Tipo de producto no válido"
  └─ "No se puede cancelar una orden ya completada"
```

---

## 6️⃣ PERMISOS DE SEGURIDAD (@PreAuthorize)

| Endpoint | Método | Permisos | Roles |
|----------|--------|----------|-------|
| POST /api/orders | createOrder | order:write | ADMIN, WAITER, CUSTOMER |
| GET /api/orders/{page}/page | getAllOrders | order:read | ADMIN, WAITER, KITCHEN |
| GET /api/orders/{id} | getOrderById | order:read | ADMIN, WAITER, KITCHEN |
| PUT /api/orders/{id} | updateOrder | order:write | ADMIN, WAITER, KITCHEN |
| PATCH /api/orders/{id}/cancel | cancelOrder | order:write | ADMIN, WAITER |
| DELETE /api/orders/{id} | deleteOrder | order:delete | ADMIN |

---

## 7️⃣ EJEMPLO DE USO EN CÓDIGO

```java
// Crear orden
CreateOrderDto orderDto = new CreateOrderDto(
    OrderChannel.ONLINE,
    "CUST-001",
    null,
    null,
    List.of(
        new CreateOrderItemDTO("DISH-001", "DISH", 2, "sin picante"),
        new CreateOrderItemDTO("DRINK-005", "DRINK", 1, "con hielo"),
        new CreateOrderItemDTO("ADD-003", "ADDITION", 3, null)
    )
);

// POST a /api/orders
ResponseDTO<String> response = restTemplate.postForObject(
    "/api/orders",
    orderDto,
    ResponseDTO.class
);

String orderId = response.data();  // "ORD-12345"

// Obtener detalle
ResponseDTO<GetOrderDetailDTO> detail = restTemplate.getForObject(
    "/api/orders/" + orderId,
    ResponseDTO.class
);

// Actualizar estado
UpdateOrderDTO updateDto = new UpdateOrderDTO(
    OrderStatus.IN_PROGRESS,
    null,
    null
);

restTemplate.put("/api/orders/" + orderId, updateDto);

// Cancelar orden
restTemplate.patchForObject("/api/orders/" + orderId + "/cancel", null, ResponseDTO.class);

// Eliminar orden
restTemplate.delete("/api/orders/" + orderId);
```

---

## 8️⃣ CARACTERÍSTICAS CLAVE

✅ **Records para DTOs**: Inmutabilidad y concisión  
✅ **MapStruct para mapeos**: Automático en compile-time  
✅ **@Transactional**: Atomicidad en operaciones  
✅ **Paginación**: 10 items por página  
✅ **Genéricos `<T>`**: Soporte polimórfico para productos  
✅ **Logging**: DEBUG y INFO para rastreo  
✅ **Validaciones**: En DTOs y en service  
✅ **Seguridad RBAC**: Permisos por rol y autoridad  
✅ **Excepciones personalizadas**: Mejor error handling  
✅ **ResponseDTO**: Respuestas consistentes  

---

## 9️⃣ ESTRUCTURA DE CARPETAS

```
orders/
├── controller/
│   └── OrderController.java
├── service/
│   ├── OrderService.java (interfaz)
│   └── impl/
│       └── OrderServiceImpl.java
├── mapper/
│   └── OrderMapper.java (MapStruct)
├── dto/
│   ├── Order/
│   │   ├── CreateOrderDto.java
│   │   ├── GetOrdersDTO.java
│   │   ├── GetOrderDetailDTO.java
│   │   └── UpdateOrderDTO.java
│   ├── orderitem/
│   │   ├── CreateOrderItemDTO.java
│   │   └── GetOrderItemDTO.java
│   └── ResponseDTO.java
├── model/
│   ├── Order.java (entity)
│   ├── OrderItem.java (entity genérica)
│   └── enums/
│       ├── OrderStatus.java
│       └── OrderChannel.java
├── repository/
│   ├── OrderRepository.java
│   ├── OrderItemRepository.java
│   └── (ProductRepositories)
├── exceptions/
│   └── OrderNotFoundException.java
└── payment/
    ├── (Payment logic)
    └── PaymentService.java
```

---

## 🔟 PRÓXIMOS PASOS

✅ Tests unitarios para OrderServiceImpl  
✅ Tests de integración para OrderController  
✅ Documentación API con Swagger/OpenAPI  
✅ Manejo de errores global (ExceptionHandler)  
✅ Auditoría de operaciones  
✅ Histórico de cambios de orden  


