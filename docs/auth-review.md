# Revisión de autenticación y autorización (frontend + backend)

## Backend (estado actual)

### Endpoints reales de auth
- `POST /api/auth/login`
  - Request DTO: `{ email: string, password: string }`
  - Response DTO: `{ accessToken: string }`
- `POST /api/auth/register`
  - Request DTO: `{ email: string, displayName: string, password: string, confirmPassword: string }`
  - Response DTO: `{ accessToken: string }`
- `GET /api/auth/me`
  - Requiere JWT válido.
  - Devuelve perfil del usuario autenticado (`id`, `email`, `displayName`, `role`, `roles`).

### Seguridad JWT
- Firma: `RS256`.
- Claims relevantes:
  - `sub` (UUID del usuario)
  - `email`
  - `displayName`
  - `roles` (array de roles)
  - `exp` (timestamp unix en segundos)
- Expiración configurable (`app.security.jwt.expiration-seconds`, default 3600s).

### Reglas de autorización vigentes
- Públicos:
  - `POST /api/auth/**`
  - `GET /api/health` y `/actuator/health`
- Tickets:
  - `POST /api/tickets` -> `USER|AGENT|ADMIN`
  - `GET /api/tickets/me` -> `USER|AGENT|ADMIN`
  - `GET /api/tickets` -> `AGENT|ADMIN`
  - `GET /api/tickets/{id}` -> `USER|AGENT|ADMIN` (con validación de ownership para `USER`)
  - `PATCH /api/tickets/{id}/status` -> `AGENT|ADMIN`
  - `PATCH /api/tickets/{id}/assignment/me` -> `AGENT|ADMIN`
  - `POST /api/tickets/{id}/comments` -> autenticado
- Administración:
  - `/api/admin/**` -> `ADMIN`
- Resto de `/api/**` -> autenticado.

## Frontend (estado actual)

### Flujo implementado
- Login y registro contra `/api/auth/login` y `/api/auth/register`.
- Consulta de perfil con `/api/auth/me` cuando aplica.
- Token en `localStorage` (`ticketing_access_token`).
- Parseo local del JWT para bootstrap de sesión (`sub`, `roles`, `exp`).
- `ProtectedRoute` redirige a `/login` si no hay sesión válida.
- `RequireRole` redirige a `/forbidden` en acceso no autorizado.

### Fortalezas actuales
- Contrato frontend-backend alineado para auth y perfil.
- Reglas de autorización backend y guards frontend consistentes.
- Seguridad stateless (Bearer JWT) adaptada al contexto SPA.

## Mejoras recomendadas (siguiente iteración)
1. Incorporar documentación OpenAPI/Swagger para visibilidad de contratos.
2. Valorar estrategia de refresh token (si se busca mejor UX en sesiones largas).
3. Ampliar E2E de rutas protegidas y escenarios de permisos por rol.
