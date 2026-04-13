# API HTTP del proyecto Ticketing (estado actual)

> Este documento sustituye el enfoque de “proposal” y pasa a ser la referencia práctica de endpoints actuales del backend.

## 1) Convenciones generales

- Base path de la API: `/api`
- Autenticación: Bearer JWT (`Authorization: Bearer <token>`)
- Roles funcionales: `USER`, `AGENT`, `ADMIN`
- Errores: gestionados por el `GlobalExceptionHandler` del backend

## 2) Autenticación (`/api/auth`)

### `POST /api/auth/login`
- Público.
- Request:
```json
{ "email": "user@local.test", "password": "password" }
```
- Response:
```json
{ "accessToken": "<jwt>" }
```

### `POST /api/auth/register`
- Público.
- Request:
```json
{
  "email": "new@local.test",
  "displayName": "Nuevo Usuario",
  "password": "password",
  "confirmPassword": "password"
}
```
- Response:
```json
{ "accessToken": "<jwt>" }
```

### `GET /api/auth/me`
- Requiere token válido.
- Devuelve perfil del usuario autenticado (id/email/displayName/rol).

## 3) Tickets (`/api/tickets`)

### Estados soportados
`OPEN | IN_PROGRESS | ON_HOLD | RESOLVED`

### Prioridades soportadas
`LOW | MEDIUM | HIGH`

### `POST /api/tickets`
- Roles: `USER | AGENT | ADMIN`
- Crea ticket.
- Request incluye `title`, `description`, `categoryId`, `priority`.

### `GET /api/tickets/me`
- Roles: `USER | AGENT | ADMIN`
- Listado paginado de tickets del usuario autenticado.
- Query params:
  - `status` (opcional)
  - `q` (opcional)
  - `page` (default `0`)
  - `size` (default `20`, max `100`)

### `GET /api/tickets`
- Roles: `AGENT | ADMIN`
- Listado paginado de cola operativa.
- Query params:
  - `scope`: `UNASSIGNED | MINE | OTHERS | ALL` (default `MINE`)
  - `status` (opcional)
  - `q` (opcional)
  - `page`, `size`

### `GET /api/tickets/{id}`
- Roles: `USER | AGENT | ADMIN`
- Detalle de ticket.
- Regla de acceso:
  - `USER`: solo sus propios tickets.
  - `AGENT/ADMIN`: acceso completo.

### `PATCH /api/tickets/{id}/status`
- Roles: `AGENT | ADMIN`
- Cambia estado de ticket (transiciones validadas por dominio).

### `PATCH /api/tickets/{id}/assignment/me`
- Roles: `AGENT | ADMIN`
- Asigna el ticket al usuario autenticado.

### `POST /api/tickets/{id}/comments`
- Requiere autenticación.
- Añade comentario al ticket.

## 4) Categorías

### `GET /api/categories`
- Requiere autenticación.
- Devuelve categorías activas para creación de tickets.

## 5) Administración (`/api/admin`)

> Todas las rutas `/api/admin/**` requieren rol `ADMIN`.

### `GET /api/admin/categories`
Lista categorías.

### `POST /api/admin/categories`
Crea categoría.

### `PATCH /api/admin/categories/{categoryId}`
Actualiza nombre y estado (`isActive`).

### `GET /api/admin/users`
Lista usuarios.

### `PATCH /api/admin/users/{userId}/active`
Activa/desactiva usuario.

## 6) Salud

### `GET /api/health`
- Público.
- Respuesta esperada: `ok`.

## 7) Formato de paginación

Para endpoints paginados de tickets se usa:

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "total": 0
}
```

## 8) Notas de evolución

- Si se añaden endpoints nuevos de tickets, deben actualizarse aquí como fuente única de estado API.
- En el siguiente paso del proyecto, esta documentación convivirá con OpenAPI/Swagger para exploración interactiva.
