# Verificación de Comunicación Frontend-Backend para Adiciones

## Problemas Encontrados y Corregidos

### 1. DTO GetAdditionDTO - Faltaban campos
**Problema:** El frontend esperaba `units` y `availableUnits` pero el backend solo enviaba `id`, `name`, `photo`, `price`.

**Solución:** 
- ✅ Agregado campo `units` al DTO
- ✅ Agregado campo `availableUnits` calculado (units - reservedUnits)
- ✅ Actualizado mapper para calcular `availableUnits` automáticamente

### 2. DTO GetAdditionDetailDTO - Faltaba availableUnits
**Problema:** El frontend esperaba `availableUnits` para mostrar las unidades disponibles reales.

**Solución:**
- ✅ Agregado campo `availableUnits` al DTO
- ✅ Actualizado ShowAdditionDetailMapper para calcular `availableUnits` (units - reservedUnits)

### 3. Campo reservedUnits faltante en la entidad
**Problema:** La base de datos tenía la columna `reserved_units` NOT NULL pero la entidad Java no la tenía.

**Solución:**
- ✅ Agregado campo `reservedUnits` a la entidad Addition con valor por defecto 0
- ✅ Creada migración V8 para agregar default value en BD
- ✅ Actualizado mapper para inicializar en 0 al crear nuevas adiciones

### 4. Imports innecesarios
**Problema:** El controller tenía imports no utilizados.

**Solución:**
- ✅ Limpiados imports innecesarios en AdditionController
- ✅ Limpiados imports innecesarios en GetAdditionDTO

## Estructura de DTOs Actualizada

### Frontend → Backend (Crear/Actualizar)

```typescript
// CreateAdditionDTO / UpdateAdditionDTO
{
  name: string;           // ✅ Coincide
  description: string;    // ✅ Coincide
  price: number;          // ✅ Coincide (double en Java)
  units: number;          // ✅ Coincide (int en Java)
  minimumStock: number;   // ✅ Coincide (int en Java)
}
```

### Backend → Frontend (Listado)

```typescript
// GetAdditionDTO
{
  id: string;             // ✅ Coincide
  name: string;           // ✅ Coincide
  photo: string;          // ✅ Coincide (primera foto o null)
  price: number;          // ✅ Coincide
  units: number;          // ✅ AGREGADO
  availableUnits: number; // ✅ AGREGADO (calculado: units - reservedUnits)
}
```

### Backend → Frontend (Detalle)

```typescript
// GetAdditionDetailDTO
{
  id: string;             // ✅ Coincide
  name: string;           // ✅ Coincide
  description: string;    // ✅ Coincide
  photos: string[];       // ✅ Coincide
  price: number;          // ✅ Coincide
  units: number;          // ✅ Coincide
  reservedUnits: number;  // ✅ AGREGADO
  availableUnits: number; // ✅ AGREGADO (calculado: units - reservedUnits)
  minimumStock: number;   // ✅ Coincide
}
```

## Endpoints Verificados

| Método | Endpoint | Frontend | Backend | Estado |
|--------|----------|----------|---------|--------|
| GET | `/api/additions/{page}/page` | ✅ | ✅ | ✅ Coincide |
| GET | `/api/additions/{id}` | ✅ | ✅ | ✅ Coincide |
| POST | `/api/additions` | ✅ | ✅ | ✅ Coincide |
| PUT | `/api/additions/{id}` | ✅ | ✅ | ✅ Coincide |
| DELETE | `/api/additions/{id}` | ✅ | ✅ | ✅ Coincide |
| PATCH | `/api/additions/{id}/add` | ✅ | ✅ | ✅ Coincide |
| PATCH | `/api/additions/{id}/discount` | ✅ | ✅ | ✅ Coincide |

## Flujo de Edición Verificado

### 1. Cargar datos para editar
```typescript
// Frontend llama:
additionService.getAdditionById(id)

// Backend responde con GetAdditionDetailDTO:
{
  id: "uuid",
  name: "Queso Extra",
  description: "Queso mozzarella adicional",
  photos: ["url1", "url2"],
  price: 2500,
  units: 20,              // ✅ Total de unidades
  reservedUnits: 5,       // ✅ Unidades reservadas en órdenes
  availableUnits: 15,     // ✅ Calculado automáticamente (20 - 5)
  minimumStock: 10
}

// Frontend carga el formulario:
this.additionForm.patchValue({
  name: addition.name,           // ✅
  description: addition.description, // ✅
  price: addition.price,         // ✅
  units: addition.units,         // ✅
  minimumStock: addition.minimumStock // ✅
});
```

### 2. Actualizar adición
```typescript
// Frontend envía UpdateAdditionDTO:
{
  name: "Queso Extra Premium",
  description: "Queso mozzarella premium",
  price: 3000,
  units: 25,
  minimumStock: 15
}

// Backend valida y actualiza
// ✅ reservedUnits NO se modifica (se mantiene en 5)
// ✅ availableUnits se recalcula automáticamente (25 - 5 = 20)
```

## Validaciones

### Backend (Java)
- ✅ `name`: 1-100 caracteres, no vacío
- ✅ `description`: máximo 500 caracteres, no vacío
- ✅ `price`: positivo, no nulo
- ✅ `units`: positivo, no nulo
- ✅ `minimumStock`: mínimo 0, no nulo

### Frontend (TypeScript)
- ✅ `name`: 1-100 caracteres, requerido
- ✅ `description`: máximo 500 caracteres, requerido
- ✅ `price`: mínimo 0.01, requerido
- ✅ `units`: mínimo 0, requerido
- ✅ `minimumStock`: mínimo 0, requerido

## Pruebas Recomendadas

1. **Crear nueva adición**
   - ✅ Verificar que `reservedUnits` se inicializa en 0
   - ✅ Verificar que `availableUnits` = `units`
   - ✅ Verificar que el precio se envía correctamente

2. **Editar adición existente**
   - ✅ Verificar que todos los campos se cargan correctamente
   - ✅ Verificar que el precio se muestra correctamente
   - ✅ Verificar que `reservedUnits` no se modifica al editar
   - ✅ Verificar que `availableUnits` se recalcula correctamente

3. **Listar adiciones**
   - ✅ Verificar que `units` y `availableUnits` se muestran
   - ✅ Verificar que el precio se muestra correctamente

4. **Ver detalle**
   - ✅ Verificar que `reservedUnits` se muestra
   - ✅ Verificar que `availableUnits` se calcula correctamente
   - ✅ Verificar que todos los campos están presentes

## Notas Importantes

- **availableUnits** es un campo calculado, NO se almacena en BD
- **reservedUnits** se gestiona automáticamente por el sistema de órdenes
- Al editar una adición, **NO se debe modificar reservedUnits** manualmente
- El precio se envía como `number` en frontend y se recibe como `double` en backend
