# 💳 Guía de Integración de Wompi

## Descripción General

Wompi es la pasarela de pagos para Colombia. Esta guía describe la integración completa de Wompi en el módulo de órdenes de Smart Restaurant, permitiendo procesar pagos con tarjetas de crédito y débito en pesos colombianos (COP).

---

## 📋 Tabla de Contenidos

1. [Configuración Inicial](#configuración-inicial)
2. [Variables de Entorno](#variables-de-entorno)
3. [Flujo de Pago](#flujo-de-pago)
4. [Endpoints de API](#endpoints-de-api)
5. [Ejemplos de Uso](#ejemplos-de-uso)
6. [Manejo de Errores](#manejo-de-errores)
7. [Testing](#testing)
8. [Producción](#producción)

---

## 🔧 Configuración Inicial

### 1. Obtener Credenciales de Wompi

1. Ir a [Wompi Console](https://console.wompi.co/)
2. Registrarse o iniciar sesión
3. Crear una aplicación
4. Obtener:
   - **API Key** (privada)
   - **Public Key** (pública)

### 2. Actualizar application.yml

El archivo `application.yml` ya contiene la configuración de Wompi:

```yaml
wompi:
  api:
    key: ${WOMPI_API_KEY:}
    public-key: ${WOMPI_PUBLIC_KEY:}
    environment: ${WOMPI_ENVIRONMENT:test}
    url-test: https://staging.wompi.co/api
    url-production: https://production.wompi.co/api
```

---

## 🌍 Variables de Entorno

### Development (Testing)

```bash
WOMPI_API_KEY=test_api_key_from_console
WOMPI_PUBLIC_KEY=test_public_key_from_console
WOMPI_ENVIRONMENT=test
```

### Production

```bash
WOMPI_API_KEY=prod_api_key_from_console
WOMPI_PUBLIC_KEY=prod_public_key_from_console
WOMPI_ENVIRONMENT=production
```

---

## 💰 Flujo de Pago

### Flujo Completo

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Cliente inicia orden en la aplicación                    │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│ 2. Cliente selecciona método de pago: Wompi                │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│ 3. Cliente ingresa datos de tarjeta en Wompi.js            │
│    (NUNCA se envían datos de tarjeta al backend)           │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│ 4. Wompi.js genera token con datos de tarjeta              │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│ 5. Frontend envía token a backend                          │
│    POST /api/payments/wompi/confirm                        │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│ 6. Backend crea transacción en Wompi API                   │
│    WompiPaymentClient.createTransaction()                  │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│ 7. Wompi API procesa transacción                            │
│    (Comunica con banco del cliente)                         │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│ 8. Wompi retorna estado: APPROVED/PENDING/DECLINED         │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│ 9. Backend guarda Payment en BD con ID de transacción      │
│    PaymentServiceImpl.confirmPaymentWithWompi()            │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│ 10. Backend retorna respuesta al cliente                    │
│     WompiPaymentResponseDTO                                │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│ 11. Frontend muestra confirmación o error                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔌 Endpoints de API

### 1. Crear Pago Tradicional (Efectivo, Tarjeta Física)

```http
POST /api/payments
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderId": "order-123",
  "customerId": 1,
  "amount": 50000.0,
  "paymentMethod": "EFECTIVO",
  "notes": "Pago en efectivo"
}
```

**Respuesta:**
```json
{
  "data": "payment-uuid-456",
  "error": false
}
```

---

### 2. Confirmar Pago con Wompi

```http
POST /api/payments/wompi/confirm
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderId": "order-123",
  "customerId": 1,
  "wompiToken": "eJydUsFuwjAM_ZWLT...",
  "amount": 5000000,
  "description": "Pago de orden de comida",
  "customerEmail": "cliente@example.com",
  "customerPhone": "+573001234567",
  "notes": "Orden especial"
}
```

**Parámetros:**
- `orderId` (String): ID de la orden
- `customerId` (Long): ID del cliente
- `wompiToken` (String): Token generado por Wompi.js
- `amount` (long): Monto en **centavos COP** (5000000 = $50,000 COP)
- `description` (String): Descripción del pago
- `customerEmail` (String): Email del cliente
- `customerPhone` (String): Teléfono (formato: +57XXXXXXXXXX)
- `notes` (String, opcional): Notas adicionales

**Respuesta Exitosa:**
```json
{
  "data": {
    "paymentId": "payment-uuid",
    "orderId": "order-123",
    "wompiTransactionId": "wompi-transaction-123",
    "status": "APPROVED",
    "amount": 5000000,
    "currency": "COP",
    "processedAt": "2026-04-09T16:30:00",
    "message": "Pago procesado exitosamente con Wompi",
    "paymentMethodType": "CARD",
    "customerEmail": "cliente@example.com"
  },
  "error": false
}
```

---

### 3. Obtener Todos los Pagos

```http
GET /api/payments
Authorization: Bearer <token>
```

**Respuesta:**
```json
{
  "data": [
    {
      "id": "payment-123",
      "orderId": "order-456",
      "amount": 50000.0,
      "paymentMethod": "WOMPI",
      "status": "CONFIRMED",
      "createdAt": "2026-04-09T16:00:00"
    }
  ],
  "error": false
}
```

---

### 4. Obtener Detalle de Pago

```http
GET /api/payments/{paymentId}
Authorization: Bearer <token>
```

---

### 5. Reembolsar Pago de Wompi

```http
POST /api/payments/{paymentId}/wompi/refund
Authorization: Bearer <token>
```

**Respuesta:**
```json
{
  "data": "Reembolso procesado exitosamente. Refund ID: wompi-refund-123",
  "error": false
}
```

---

## 💡 Ejemplos de Uso

### JavaScript - Generar Token con Wompi.js

```javascript
// Incluir Wompi.js en HTML
<script src="https://cdn.wompi.co/v1/"></script>

// Cargar Wompi
const wompi = new Wompi({
  publicKey: "YOUR_PUBLIC_KEY_HERE",
  currency: "COP"
});

// Montar formulario de pago
wompi.mount({
  container: '#payment-form'
});

// Generar token
const token = await wompi.tokenize({
  cardNumber: "4242 4242 4242 4242",
  expiry: "12/25",
  cvc: "123",
  cardholderName: "John Doe"
});

// Enviar token al backend
fetch('/api/payments/wompi/confirm', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${authToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    orderId: orderData.id,
    customerId: currentUser.id,
    wompiToken: token.id,
    amount: orderData.totalInCents,
    description: `Pago de orden ${orderData.id}`,
    customerEmail: currentUser.email,
    customerPhone: currentUser.phone,
    notes: "Pago con tarjeta"
  })
});
```

### Java - Confirmar Pago

```java
// Inyectar el servicio
@Autowired
private PaymentService paymentService;

// Preparar DTO
ConfirmPaymentWithWompiDTO paymentDto = new ConfirmPaymentWithWompiDTO(
  "order-123",
  1L,
  "eJydUsFuwjAM...",
  5000000,
  "Pago de comida",
  "cliente@example.com",
  "+573001234567",
  "Nota especial"
);

// Procesar pago
WompiPaymentResponseDTO response = paymentService.confirmPaymentWithWompi(paymentDto);

// Validar respuesta
if ("APPROVED".equals(response.status())) {
  System.out.println("✅ Pago aprobado: " + response.wompiTransactionId());
} else if ("PENDING".equals(response.status())) {
  System.out.println("⏳ Pago pendiente");
} else {
  System.out.println("❌ Pago rechazado");
}
```

---

## ⚠️ Manejo de Errores

### Estados Posibles en Wompi

| Status | Significado | Acción |
|--------|-------------|--------|
| `APPROVED` | Pago aprobado | Orden confirmada ✅ |
| `PENDING` | Esperando confirmación | Mostrar estado pendiente ⏳ |
| `DECLINED` | Pago rechazado | Permitir reintentar ❌ |
| `ERROR` | Error en transacción | Contactar soporte |

### Códigos de Error Comunes

```
100 - Monto inválido
101 - Cliente no encontrado
102 - Orden no encontrada
103 - La orden ya tiene un pago
104 - Token de Wompi inválido
105 - Email del cliente requerido
106 - Teléfono del cliente requerido
```

### Logs de Debugging

Todos los logs incluyen prefijos con emojis para fácil identificación:

- 💳 `[WOMPI]` - Acción de Wompi
- ✅ - Operación exitosa
- ❌ - Error o fallo
- 🔄 - Procesando/en progreso
- 📋 - Consulta de datos
- 💰 - Operación de reembolso

**Ejemplo de log:**
```
2026-04-09 16:30:45 INFO  💳 [WOMPI] Iniciando confirmación de pago con Wompi
2026-04-09 16:30:45 INFO  📊 [WOMPI] Orden: order-123, Cliente: 1, Monto: 5000000 centavos COP
2026-04-09 16:30:46 INFO  🔄 [WOMPI] Creando transacción en Wompi...
2026-04-09 16:30:48 INFO  ✅ [WOMPI] Transacción creada en Wompi: transactionId=wompi-123, status=APPROVED
2026-04-09 16:30:48 INFO  ✅ [WOMPI] Pago guardado en BD: paymentId=payment-456
```

---

## 🧪 Testing

### Archivo HTTP para Testing

Usar el archivo incluido: `src/main/resources/ApiTests/Payment.http`

**Tokens de Prueba:**
```
eJydUsFuwjAM_ZWLT  - Token de prueba válido
eJydUsFuwjAM_WJLT  - Otro token de prueba
```

### Pruebas Manuales

1. **Test de Pago Exitoso:**
   ```bash
   curl -X POST http://localhost:8080/api/payments/wompi/confirm \
     -H "Authorization: Bearer tu_token_jwt" \
     -H "Content-Type: application/json" \
     -d '{
       "orderId": "test-order-1",
       "customerId": 1,
       "wompiToken": "eJydUsFuwjAM_ZWLT",
       "amount": 100000,
       "description": "Test payment",
       "customerEmail": "test@example.com",
       "customerPhone": "+573001234567"
     }'
   ```

2. **Test de Reembolso:**
   ```bash
   curl -X POST http://localhost:8080/api/payments/payment-id-123/wompi/refund \
     -H "Authorization: Bearer tu_token_jwt"
   ```

---

## 🚀 Producción

### Antes de Ir a Producción

1. ✅ Cambiar `WOMPI_ENVIRONMENT` a `production`
2. ✅ Usar credenciales de producción de Wompi
3. ✅ Usar URL de producción: `https://production.wompi.co/api`
4. ✅ Habilitar validaciones de seguridad (HTTPS obligatorio)
5. ✅ Verificar SSL certificates
6. ✅ Implementar webhooks de Wompi para confirmaciones asincrónicas
7. ✅ Agregar rate limiting a endpoints de pago
8. ✅ Implementar monitoreo y alertas de errores de pago

### Configuración de Producción

```yaml
# application-production.yml
wompi:
  api:
    key: ${WOMPI_API_KEY}  # Usar variable de entorno segura
    public-key: ${WOMPI_PUBLIC_KEY}  # Usar variable de entorno segura
    environment: production
    url-test: https://staging.wompi.co/api
    url-production: https://production.wompi.co/api
```

### Monitoreo

- Monitorear logs con emoji `🔴 [WOMPI]` para errores
- Alertas automáticas para transacciones con estado `DECLINED`
- Dashboard de transacciones en tiempo real
- Reportes diarios de pagos procesados

---

## 📚 Referencias

- [Wompi Documentation](https://wompi.co/docs)
- [Wompi SDK](https://github.com/wompicorp/wompi-sdk)
- [Wompi Console](https://console.wompi.co/)

---

## 🤝 Soporte

Para problemas con la integración:

1. Revisar logs con emojis `💳 [WOMPI]`
2. Verificar que credenciales de Wompi estén correctas
3. Validar que el ambiente sea `test` o `production` según corresponda
4. Contactar a soporte de Wompi: support@wompi.co

---

**Última actualización:** 9 de abril de 2026
**Versión:** 1.0
**Estado:** ✅ Production Ready

