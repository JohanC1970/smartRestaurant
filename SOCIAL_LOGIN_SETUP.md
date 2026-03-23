# Configuración de Social Login - SmartRestaurante Backend

## 📋 Resumen

El sistema de Social Login permite a los usuarios iniciar sesión o registrarse usando sus cuentas de:
- Google
- Facebook  
- GitHub

## 🔧 Configuración de Proveedores

### 1. Google OAuth

#### Paso 1: Crear Proyecto en Google Cloud Console

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita la API de Google+ (Google+ API)

#### Paso 2: Crear Credenciales OAuth 2.0

1. Ve a "APIs & Services" > "Credentials"
2. Click en "Create Credentials" > "OAuth client ID"
3. Selecciona "Web application"
4. Configura:
   - **Authorized JavaScript origins**: 
     - `http://localhost:4200` (desarrollo)
     - `https://tudominio.com` (producción)
   - **Authorized redirect URIs**:
     - `http://localhost:4200` (desarrollo)
     - `https://tudominio.com` (producción)

#### Paso 3: Obtener Client ID

1. Copia el "Client ID" generado
2. Agrégalo a tu archivo `.env` o variables de entorno:

```bash
GOOGLE_CLIENT_ID=tu-client-id-aqui.apps.googleusercontent.com
```

---

### 2. Facebook OAuth

#### Paso 1: Crear App en Facebook Developers

1. Ve a [Facebook Developers](https://developers.facebook.com/)
2. Click en "My Apps" > "Create App"
3. Selecciona "Consumer" como tipo de app
4. Completa la información básica

#### Paso 2: Configurar Facebook Login

1. En el dashboard de tu app, agrega el producto "Facebook Login"
2. Ve a "Settings" > "Basic"
3. Copia el "App ID"
4. En "Facebook Login" > "Settings", configura:
   - **Valid OAuth Redirect URIs**:
     - `http://localhost:4200` (desarrollo)
     - `https://tudominio.com` (producción)

#### Paso 3: Configurar Variables de Entorno

```bash
FACEBOOK_APP_ID=tu-app-id-aqui
```

---

### 3. GitHub OAuth

#### Paso 1: Crear OAuth App en GitHub

1. Ve a [GitHub Settings](https://github.com/settings/developers)
2. Click en "OAuth Apps" > "New OAuth App"
3. Completa:
   - **Application name**: SmartRestaurante
   - **Homepage URL**: `http://localhost:4200` o tu dominio
   - **Authorization callback URL**: `http://localhost:4200/auth/callback`

#### Paso 2: Obtener Credenciales

1. Copia el "Client ID"
2. Genera un "Client Secret"
3. Agrégalos a tus variables de entorno:

```bash
GITHUB_CLIENT_ID=tu-client-id-aqui
GITHUB_CLIENT_SECRET=tu-client-secret-aqui
```

---

## 🔐 Configuración del Backend

### Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto backend o configura las variables en tu sistema:

```bash
# Google OAuth
GOOGLE_CLIENT_ID=123456789-abcdefghijklmnop.apps.googleusercontent.com

# Facebook OAuth
FACEBOOK_APP_ID=123456789012345

# GitHub OAuth
GITHUB_CLIENT_ID=Iv1.abcdef123456
GITHUB_CLIENT_SECRET=abcdef123456789abcdef123456789abcdef1234
```

### application.yml

Las configuraciones ya están en `application.yml`:

```yaml
social:
  google:
    client-id: ${GOOGLE_CLIENT_ID:}
  facebook:
    app-id: ${FACEBOOK_APP_ID:}
  github:
    client-id: ${GITHUB_CLIENT_ID:}
    client-secret: ${GITHUB_CLIENT_SECRET:}
```

---

## 🚀 Flujo de Autenticación

### Diagrama de Flujo

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│   Frontend  │─────▶│   Backend    │─────▶│  Provider   │
│   Angular   │      │  Spring Boot │      │ (Google/FB) │
└─────────────┘      └──────────────┘      └─────────────┘
      │                      │                      │
      │  1. Obtener token    │                      │
      │◀─────────────────────┼──────────────────────┘
      │                      │
      │  2. POST /auth/social-login
      ├─────────────────────▶│
      │  {                   │
      │    provider: "GOOGLE"│
      │    accessToken: "..." │
      │  }                   │
      │                      │
      │                      │  3. Validar token
      │                      ├──────────────────────▶
      │                      │  GET /oauth2/v3/userinfo
      │                      │  Authorization: Bearer ...
      │                      │
      │                      │  4. Obtener info user
      │                      │◀──────────────────────
      │                      │  {
      │                      │    sub: "123",
      │                      │    email: "user@gmail.com",
      │                      │    given_name: "John",
      │                      │    family_name: "Doe"
      │                      │  }
      │                      │
      │                      │  5. Crear/Actualizar user
      │                      │  - Buscar cuenta social
      │                      │  - Buscar usuario por email
      │                      │  - Crear nuevo si no existe
      │                      │  - Vincular cuenta social
      │                      │
      │  6. Retornar tokens  │
      │◀─────────────────────│
      │  {                   │
      │    accessToken: "...",
      │    refreshToken: "...",
      │    message: "Login exitoso"
      │  }                   │
```

### Lógica del Backend

1. **Recibe token del frontend**: El frontend obtiene el token del proveedor y lo envía al backend
2. **Valida token**: El backend valida el token con la API del proveedor
3. **Obtiene información**: Extrae email, nombre, foto de perfil, etc.
4. **Busca cuenta social**: Verifica si ya existe una cuenta social vinculada
5. **Busca usuario por email**: Si no hay cuenta social, busca usuario con ese email
6. **Crea o vincula**:
   - Si existe usuario: Vincula la cuenta social
   - Si no existe: Crea nuevo usuario con rol CUSTOMER
7. **Genera tokens JWT**: Crea access token y refresh token propios
8. **Retorna respuesta**: Envía tokens al frontend

---

## 🗄️ Estructura de Base de Datos

### Tabla: social_accounts

```sql
CREATE TABLE social_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,  -- GOOGLE, FACEBOOK, GITHUB
    provider_id VARCHAR(255) NOT NULL,  -- ID del usuario en el proveedor
    profile_picture_url VARCHAR(500),
    linked_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (provider, provider_id)
);
```

### Relación con Users

- Un usuario puede tener múltiples cuentas sociales vinculadas
- Una cuenta social pertenece a un solo usuario
- Si se elimina el usuario, se eliminan sus cuentas sociales (CASCADE)

---

## 🧪 Testing

### Probar con Postman

#### 1. Obtener Token de Google (Manual)

1. Ve a [OAuth 2.0 Playground](https://developers.google.com/oauthplayground/)
2. Selecciona "Google OAuth2 API v2"
3. Autoriza y obtén el access token

#### 2. Llamar al Endpoint

```http
POST http://localhost:8080/auth/social-login
Content-Type: application/json

{
  "provider": "GOOGLE",
  "accessToken": "ya29.a0AfH6SMB..."
}
```

#### 3. Respuesta Esperada

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

## ⚠️ Consideraciones de Seguridad

### 1. Validación de Tokens

- **SIEMPRE** validar tokens con la API del proveedor
- **NUNCA** confiar en información del frontend sin validar
- Los tokens tienen tiempo de expiración

### 2. HTTPS Obligatorio

- En producción, **SIEMPRE** usar HTTPS
- Los proveedores OAuth requieren HTTPS para callbacks

### 3. Secrets

- **NUNCA** exponer client secrets en el frontend
- Usar variables de entorno
- No commitear secrets en Git

### 4. Rate Limiting

- Implementar límite de intentos
- Prevenir ataques de fuerza bruta

---

## 🐛 Troubleshooting

### Error: "Token de Google inválido"

**Causas posibles:**
- Token expirado
- Token de otro proyecto/app
- Client ID incorrecto

**Solución:**
- Verificar que el token sea reciente
- Verificar configuración de OAuth en Google Console

### Error: "Email no disponible"

**Causas posibles:**
- Usuario no autorizó compartir email
- Configuración incorrecta de scopes

**Solución:**
- Solicitar scope `email` en el frontend
- Verificar permisos en la configuración de OAuth

### Error: "CORS"

**Causas posibles:**
- Origen no autorizado
- Configuración incorrecta de CORS

**Solución:**
- Agregar origen en `application.yml`:
  ```yaml
  cors:
    allowed-origins: http://localhost:4200
  ```

---

## 📚 Referencias

- [Google OAuth 2.0](https://developers.google.com/identity/protocols/oauth2)
- [Facebook Login](https://developers.facebook.com/docs/facebook-login)
- [GitHub OAuth](https://docs.github.com/en/developers/apps/building-oauth-apps)

---

## ✅ Checklist de Implementación

- [x] Crear entidad SocialAccount
- [x] Crear enum SocialProvider
- [x] Crear DTOs (SocialLoginRequest, SocialUserInfo)
- [x] Crear SocialAccountRepository
- [x] Implementar SocialAuthValidator
- [x] Agregar método socialLogin en AuthenticationService
- [x] Agregar endpoint POST /auth/social-login
- [x] Crear migración de base de datos
- [x] Configurar application.yml
- [x] Crear RestTemplate bean
- [ ] Configurar credenciales OAuth en proveedores
- [ ] Configurar variables de entorno
- [ ] Testing con tokens reales
- [ ] Documentación API (Swagger)
