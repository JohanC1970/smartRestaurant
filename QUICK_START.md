# 🚀 Quick Start - Social Login

## Inicio Rápido en 5 Minutos

### 1. Ejecutar Migración de Base de Datos

La migración se ejecutará automáticamente al iniciar la aplicación. Si usas Flyway/Liquibase, el script `V2__create_social_accounts_table.sql` se aplicará automáticamente.

Si prefieres ejecutarlo manualmente:

```sql
-- Conectar a tu base de datos PostgreSQL
psql -U postgres -d smart_restaurant

-- Ejecutar el script
\i src/main/resources/db/migration/V2__create_social_accounts_table.sql
```

### 2. Configurar Variables de Entorno (Opcional para Testing)

Por ahora, puedes dejar las variables vacías. El sistema funcionará sin credenciales OAuth para testing local.

```bash
# Opcional - Solo si quieres probar con proveedores reales
export GOOGLE_CLIENT_ID=""
export FACEBOOK_APP_ID=""
export GITHUB_CLIENT_ID=""
export GITHUB_CLIENT_SECRET=""
```

### 3. Iniciar el Backend

```bash
cd Backend
./mvnw spring-boot:run
```

### 4. Probar GET /auth/me

```bash
# 1. Login con usuario existente
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "Admin123!"
  }'

# 2. Copiar el accessToken de la respuesta

# 3. Obtener información del usuario
curl -X GET http://localhost:8080/auth/me \
  -H "Authorization: Bearer {TU_ACCESS_TOKEN_AQUI}"
```

### 5. Probar Social Login (Requiere Token Real)

Para obtener un token de Google para testing:

1. Ve a [OAuth 2.0 Playground](https://developers.google.com/oauthplayground/)
2. En "Step 1", selecciona "Google OAuth2 API v2" > "userinfo.email" y "userinfo.profile"
3. Click "Authorize APIs"
4. Autoriza con tu cuenta de Google
5. En "Step 2", click "Exchange authorization code for tokens"
6. Copia el "Access token"

Luego:

```bash
curl -X POST http://localhost:8080/auth/social-login \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "GOOGLE",
    "accessToken": "ya29.a0AfH6SMB..."
  }'
```

---

## ✅ Verificación

Si todo funciona correctamente, deberías ver:

### GET /auth/me
```json
{
  "id": 1,
  "firstName": "Admin",
  "lastName": "User",
  "email": "admin@example.com",
  "role": "ADMIN",
  "roleDisplayName": "Administrador",
  "status": "ACTIVE",
  "statusDisplayName": "Activo",
  "isEmailVerified": true,
  "requiresPasswordChange": false,
  "failedLoginAttempts": 0,
  "createdAt": "2024-03-06T10:00:00",
  "updatedAt": "2024-03-06T10:00:00"
}
```

### POST /auth/social-login
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login exitoso con Google",
  "is2faRequired": false,
  "requiresPasswordChange": false
}
```

---

## 🔧 Configuración Completa (Producción)

Para configurar completamente el Social Login en producción, consulta:

📖 **SOCIAL_LOGIN_SETUP.md** - Guía detallada paso a paso

---

## 🐛 Problemas Comunes

### Error: "Token de Google inválido"
- El token expiró (duran ~1 hora)
- Obtén un nuevo token del OAuth Playground

### Error: "Usuario no encontrado" en GET /auth/me
- El token JWT es inválido o expiró
- Haz login nuevamente para obtener un nuevo token

### Error: "Table 'social_accounts' doesn't exist"
- La migración no se ejecutó
- Ejecuta manualmente el script SQL

---

## 📚 Documentación Completa

- `IMPLEMENTATION_SUMMARY.md` - Resumen de implementación
- `SOCIAL_LOGIN_SETUP.md` - Configuración de OAuth
- `Frontend/BACKEND_IMPLEMENTATION_GUIDE.md` - Guía técnica detallada
- `Frontend/AUTH_MODULE_SUMMARY.md` - Resumen del frontend

---

¡Listo para usar! 🎉
