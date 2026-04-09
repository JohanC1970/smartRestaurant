# Descripción de la Arquitectura - Smart Restaurant

El sistema se implementa en dos módulos principales alineados con el **Nivel 2 (Contenedores)** del modelo C4: una aplicación móvil Android (Java) como canal del cliente y un Backend en Java (Spring Boot) que centraliza la lógica de negocio e integraciones externas.

## Estructura de Paquetes (Árbol)

```text
/smartRestaurant/
├── /mobile-app-android/ (Módulo proyectado)
│   └── /app/src/main/java/com/foodexpress/client/
│       ├── /ui/            (Activities/Fragments)
│       ├── /viewmodel/     (Lógica de presentación)
│       ├── /network/       (API client, requests)
│       ├── /model/         (DTOs y modelos)
│       └── /util/          (Helpers, constantes)
│
├── /backend/ (Módulo Actual: smartRestaurant)
│   ├── /src/main/java/com/smartRestaurant/
│   │   ├── /auth/          (Dominio de Autenticación y Usuarios)
│   │   │   ├── /controller/
│   │   │   ├── /service/
│   │   │   ├── /repository/
│   │   │   └── /model/
│   │   ├── /order/         (Dominio de Gestión de Pedidos)
│   │   │   ├── /controller/
│   │   │   ├── /service/
│   │   │   ├── /repository/
│   │   │   └── /model/
│   │   ├── /menu/          (Dominio de Gestión de Menú)
│   │   │   ├── /controller/
│   │   │   ├── /service/
│   │   │   ├── /repository/
│   │   │   └── /model/
│   │   ├── /kitchen/       (Dominio de Operaciones de Cocina)
│   │   ├── /security/      (Configuración global de Seguridad/JWT)
│   │   └── /common/        (Utilidades, excepciones y DTOs compartidos)
│   ├── /src/main/resources/
│   │   └── application.yml
│   └── pom.xml
│
└── /docs/
    ├── /C4Model/
    ├── /arquitectura/
    └── /decisiones-arquitectonicas/
```

## Componentes Principales

### Backend (Spring Boot)
El backend utiliza una arquitectura **basada en dominios**, donde cada paquete principal (`auth`, `order`, `menu`, `kitchen`) encapsula su propia lógica de negocio y persistencia, siguiendo un patrón de capas interno:
- **Controller**: Expone las APIs REST.
- **Service**: Implementa las reglas de negocio.
- **Repository**: Maneja el acceso a datos mediante Spring Data JPA.
- **Model**: Define las entidades de persistencia.

### Mobile App (Android Java)
Diseñada para actuar como el canal principal del cliente, interactuando con el Backend mediante servicios REST. Sigue el patrón **MVVM** para separar la UI de la lógica de negocio.

### Documentación
La carpeta `/docs` centraliza los artefactos de diseño, incluyendo diagramas C4 y registros de decisiones arquitectónicas (ADR).
