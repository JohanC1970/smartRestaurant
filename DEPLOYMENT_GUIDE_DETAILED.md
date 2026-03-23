# Guía de Despliegue Profesional con Docker (Detallada)

Si el despliegue anterior falló, lo más probable es que fuera por diferencias en el entorno de Java o configuración de puertos. Usar **Docker** soluciona esto porque empaqueta todo lo que tu app necesita.

## 1. Base de Datos: Neon.tech (PostgreSQL)
1.  **Crear Proyecto**: En [Neon.tech](https://neon.tech/), crea `smart-restaurant`.
2.  **Obtener URL**: Copia la Connection String en formato **JDBC**.
    *   Debe verse así: `jdbc:postgresql://ep-example-pooler.us-east-2.aws.neon.tech/neondb?sslmode=require`
3.  **Credenciales**: Anota tu `DB_USERNAME` y `DB_PASSWORD`.

## 2. Backend: Render con Docker (¡Más Estable!)
Render detectará automáticamente el archivo `Dockerfile` que acabo de crear.

1.  **Nuevo Web Service**: Conecta tu repositorio `smartRestaurant`.
2.  **Runtime**: Selecciona **Docker**. (Render ignorará Maven y usará mi archivo `Dockerfile`).
3.  **Plan**: Selecciona **Free**.
4.  **Variables de Entorno (Environment Variables)**:
    - `DB_URL`: La URL JDBC de Neon.
    - `DB_USERNAME`: Tu usuario de Neon.
    - `DB_PASSWORD`: Tu contraseña de Neon.
    - `JWT_SECRET`: Una clave larga (ej: `TuClaveSuperSecretaDeAlMenos32Caracteres`).
    - `GOOGLE_CLIENT_ID`: Tu ID de Google Console.
    - `GOOGLE_CLIENT_SECRET`: Tu Secreto de Google Console.
    - `CORS_ALLOWED_ORIGINS`: La URL que te dé Vercel (ej: `https://tu-app.vercel.app`).
    - `MAIL_PASSWORD`: Tu contraseña de aplicación de Gmail.
    - `CLOUDINARY_NAME`, `CLOUDINARY_KEY`, `CLOUDINARY_SECRET`: Tus datos de Cloudinary.

> [!TIP]
> **El problema del puerto**: Render usa un puerto variable `$PORT`. He configurado el `Dockerfile` para que tome ese puerto automáticamente y se lo pase a Spring Boot. No necesitas configurar `SERVER_PORT` manualmente.

## 3. Frontend: Vercel (Angular)
1.  **Configurar URL del Back**: 
    - Ve a `src/environments/environment.prod.ts`.
    - Asegúrate de que `apiUrl` sea la URL de Render (ej: `https://mi-backend.onrender.com/api`).
2.  **Desplegar**: Conecta tu repo `Frontend` a Vercel.
3.  **Build Settings**: Vercel detectará Angular. Asegúrate de que el "Output Directory" sea `dist/frontend` (o el nombre de tu proyecto en `angular.json`).

## 4. ¿Por qué podría fallar? (Checklist de errores)

### A. El Backend no conecta a la DB
- **Error**: `Connection refused` o `Timed out`.
- **Solución**: Verifica que la URL en Render tenga `jdbc:postgresql://` al inicio y `?sslmode=require` al final.

### B. El Frontend no puede hablar con el Backend (CORS)
- **Error**: `CORS error` en la consola del navegador.
- **Solución**: En Render, agrega la variable `CORS_ALLOWED_ORIGINS` con el valor exacto de tu URL de Vercel (sin la barra final).

### C. El botón de Google no carga
- **Error**: Se queda en blanco o da error de redirección.
- **Solución**: Ve a [Google Cloud Console](https://console.cloud.google.com/apis/credentials) y agrega la URL de Vercel en **"Orígenes de JavaScript autorizados"**.
