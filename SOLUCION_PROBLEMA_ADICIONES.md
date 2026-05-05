# Solución al Problema de Adiciones

## Problema Original
```
org.springframework.dao.DataIntegrityViolationException: could not execute statement 
[ERROR: null value in column "reserved_units" of relation "addition" violates not-null constraint]
```

## Causa Raíz
La tabla `addition` en la base de datos tenía una columna `reserved_units` NOT NULL, pero:
1. La entidad Java `Addition` no tenía el campo `reservedUnits`
2. Los DTOs no incluían campos necesarios para el frontend
3. Los mappers no estaban configurados correctamente

## Soluciones Implementadas

### 1. Entidad Addition
✅ **Agregado campo `reservedUnits`**
```java
@Column(nullable = false)
private int reservedUnits = 0;
```

### 2. Migración de Base de Datos
✅ **Creada migración V8__add_reserved_units_default_to_addition.sql**
```sql
ALTER TABLE addition ALTER COLUMN reserved_units SET DEFAULT 0;
UPDATE addition SET reserved_units = 0 WHERE reserved_units IS NULL;
```

### 3. DTOs Actualizados

#### GetAdditionDTO
✅ **Agregados campos faltantes:**
```java
public record GetAdditionDTO(
    String id,
    String name,
    String photo,
    double price,
    int units,              // ✅ AGREGADO
    int availableUnits      // ✅ AGREGADO (calculado)
)
```

#### GetAdditionDetailDTO
✅ **Agregados campos faltantes:**
```java
public record GetAdditionDetailDTO(
    String id,
    String name,
    String description,
    List<String> photos,
    double price,
    int units,
    int reservedUnits,      // ✅ AGREGADO
    int availableUnits,     // ✅ AGREGADO (calculado)
    int minimumStock
)
```

### 4. Mappers Actualizados

#### AdditionMapper
✅ **Configurado para inicializar reservedUnits:**
```java
@Mapping(target = "reservedUnits", constant = "0")
@Mapping(target = "availableUnits", expression = "java(addition.getUnits() - addition.getReservedUnits())")
```

#### ShowAdditionDetailMapper
✅ **Configurado para calcular availableUnits:**
```java
@Mapping(target = "reservedUnits", source = "reservedUnits")
@Mapping(target = "availableUnits", expression = "java(addition.getUnits() - addition.getReservedUnits())")
```

### 5. Limpieza de Código
✅ **Eliminados imports innecesarios en:**
- AdditionController
- CreateAdditionDTO
- GetAdditionDTO

## Verificación

### Compilación
```bash
./mvnw clean compile
```
✅ **Resultado:** BUILD SUCCESS

### Inicio de Aplicación
```bash
./mvnw spring-boot:run
```
✅ **Resultado:** Aplicación iniciada correctamente en puerto 8080

### Mappers Generados
✅ **Verificado que MapStruct generó:**
- `AdditionMapperImpl.java` - Con cálculo de availableUnits
- `ShowAdditionDetailMapperImpl.java` - Con cálculo de availableUnits

## Flujo Correcto Ahora

### Crear Adición
1. Frontend envía: `{ name, description, price, units, minimumStock }`
2. Backend crea con: `reservedUnits = 0` automáticamente
3. ✅ No más error de NOT NULL constraint

### Editar Adición
1. Frontend solicita detalle: GET `/api/additions/{id}`
2. Backend responde con todos los campos incluyendo:
   - `units`: Total de unidades
   - `reservedUnits`: Unidades reservadas en órdenes
   - `availableUnits`: Calculado (units - reservedUnits)
3. Frontend carga el formulario correctamente
4. Frontend envía actualización: `{ name, description, price, units, minimumStock }`
5. Backend actualiza sin modificar `reservedUnits`
6. ✅ Precio y todos los campos se manejan correctamente

### Listar Adiciones
1. Frontend solicita: GET `/api/additions/{page}/page`
2. Backend responde con:
   - `units`: Total de unidades
   - `availableUnits`: Unidades disponibles (calculado)
   - `price`: Precio correcto
3. ✅ Frontend muestra toda la información correctamente

## Campos Calculados vs Almacenados

### Almacenados en BD
- `units`: Total de unidades en inventario
- `reservedUnits`: Unidades reservadas en órdenes activas

### Calculados Automáticamente
- `availableUnits = units - reservedUnits`: Unidades realmente disponibles

## Gestión de reservedUnits

⚠️ **IMPORTANTE:** El campo `reservedUnits` es gestionado automáticamente por el sistema:
- Se incrementa cuando se crea una orden
- Se decrementa cuando se cancela una orden
- Se decrementa cuando se completa una orden
- **NO debe modificarse manualmente** al editar una adición

## Pruebas Recomendadas

1. ✅ **Crear nueva adición** - Verificar que se crea sin errores
2. ✅ **Editar adición** - Verificar que carga todos los datos correctamente
3. ✅ **Ver precio** - Verificar que el precio se muestra y edita correctamente
4. ✅ **Ver unidades disponibles** - Verificar que availableUnits se calcula bien
5. ⏳ **Crear orden con adición** - Verificar que reservedUnits se incrementa
6. ⏳ **Cancelar orden** - Verificar que reservedUnits se decrementa

## Estado Final
✅ **Backend compilado y funcionando**
✅ **Mappers generados correctamente**
✅ **Aplicación iniciada sin errores**
✅ **Comunicación Frontend-Backend verificada**
✅ **Todos los campos sincronizados**
