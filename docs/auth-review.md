# Revisión de autenticación (frontend + backend)

## Backend (estado actual)

### Endpoints reales
- `POST /api/auth/login`
  - Request DTO: `{ email: string, password: string }`
  - Response DTO: `{ accessToken: string }`
- `POST /api/auth/register`
  - Request DTO: `{ email: string, displayName: string, password: string, confirmPassword: string }`
  - Response DTO: `{ accessToken: string }`

### Seguridad JWT
- Firma: `RS256`.
- Claims relevantes en token:
  - `sub` (UUID del usuario)
  - `roles` (array con roles, ejemplo `['USER']`)
  - `exp` (timestamp unix segundos)
- Expiración configurable (por defecto 3600s).

### Reglas de autorización
- `POST /api/auth/**` → público.
- `/api/tickets` → `USER|AGENT|ADMIN`.
- `/api/tickets/*/status` → `AGENT|ADMIN`.
- Resto de `/api/**` → autenticado.

## Frontend (estado actual)

### Flujo implementado
- Login/registro contra endpoints reales `/api/auth/login` y `/api/auth/register`.
- Token almacenado en `localStorage` con clave `ticketing_access_token` cuando `remember=true`.
- Parseo local del JWT (sin validar firma) para obtener `sub`, `roles` y `exp`.
- `ProtectedRoute`:
  - Si no autenticado o token expirado, redirige a `/login`.
- Guard de roles (`RequireRole`) redirige a `/forbidden`.

### Ajustes realizados en este commit
- Sidebar dinámico por rol:
  - USER/AGENT no ven `Administración`.
  - ADMIN sí ve `Administración`.
- Tests añadidos para validar:
  - visibilidad de menú admin por rol;
  - redirect de `ProtectedRoute` a `/login` sin token.

## Valoración

### Lo que está bien
- Contrato frontend-backend consistente (DTOs y rutas de auth).
- JWT con claims necesarios para MVP (`sub`, `roles`, `exp`).
- Control de sesión correcto para SPA (stateless, Bearer token).
- Guards en frontend y reglas de autorización en backend alineadas.

### Mejoras recomendadas (no bloqueantes para MVP)
1. Añadir refresh token o estrategia de renovación para evitar relogin cada 1h.
2. Exponer endpoint `/api/auth/me` opcional para perfil canónico (aunque JWT ya cubre MVP).
3. Añadir pruebas E2E de rutas protegidas por rol cuando el flujo principal esté completo.
