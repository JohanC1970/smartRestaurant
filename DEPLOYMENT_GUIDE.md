# Guía de Despliegue Gratuito (Smart Restaurant)

Esta guía detalla cómo desplegar tu aplicación completa utilizando el nivel gratuito de servicios modernos.

## 1. Base de Datos: Neon.tech (PostgreSQL)
Neon ofrece bases de datos PostgreSQL serverless con un plan gratuito generoso.

1.  Crea una cuenta en [Neon.tech](https://neon.tech/).
2.  Crea un nuevo proyecto llamado `smart-restaurant-db`.
3.  En el Dashboard, copia la **Connection String** (selecciona el modo "Pooled Connection" para mejor rendimiento).
    *   Ejemplo: `postgresql://neondb_owner:contraseña@ep-small-lake.neon.tech/neondb`
4.  Guarda esta URL, la necesitaremos para el backend.

## 2. Backend: Render (Spring Boot)
Render permite desplegar aplicaciones Java de forma gratuita con una configuración sencilla.

1.  Crea una cuenta en [Render.com](https://render.com/).
2.  Haz clic en **New +** > **Web Service**.
3.  Conecta tu repositorio de GitHub `smartRestaurant`.
4.  Configura:
    *   **Build Command**: `./mvnw clean package -DskipTests`
    *   **Start Command**: `java -jar target/*.jar`
5.  En **Environment Variables**, agrega:
    *   `DB_URL`: La URL de JDBC (ej: `jdbc:postgresql://ep-small-lake...neon.tech/neondb?sslmode=require`)
    *   `DB_USERNAME`: Usuario de la DB de Neon.
    *   `DB_PASSWORD`: Contraseña de la DB de Neon.
    *   `JWT_SECRET`: Una clave larga y aleatoria.
    *   `MAIL_PASSWORD`: Tu contraseña de aplicación de Gmail.
    *   `GOOGLE_CLIENT_ID`: El mismo que usas localmente o uno nuevo para prod.
    *   `GOOGLE_CLIENT_SECRET`: Tu secreto de Google.

## 3. Frontend: Vercel (Angular)
Vercel es el estándar para desplegar interfaces modernas.

1.  Asegúrate de actualizar `src/environments/environment.prod.ts` con la URL de Render.
2.  En [Vercel.com](https://vercel.com/), importa el repositorio `Frontend`.
3.  Vercel detectará Angular y lo desplegará automáticamente.

## 4. Seguridad y Google Auth
**¡Muy importante!**
1.  En [Google Cloud Console](https://console.cloud.google.com/apis/credentials), edita tu cliente OAuth.
2.  Agrega la URL de tu aplicación en Vercel (ej: `https://tu-app.vercel.app`) a:
    *   **Orígenes de JavaScript autorizados**.
    *   **URIs de redireccionamiento autorizados**.
3.  ¡Listo! Ya tienes tu app en la web de forma gratuita.
