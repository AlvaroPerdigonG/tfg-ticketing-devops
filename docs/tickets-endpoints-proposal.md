# Ticketing API HTTP Reference (current state)

> This document is the current source of truth for backend endpoints.

## 1) General conventions

- API base path: `/api`
- Authentication: Bearer JWT (`Authorization: Bearer <token>`)
- Roles: `USER`, `AGENT`, `ADMIN`
- Errors are handled centrally by `GlobalExceptionHandler`

## 2) Authentication (`/api/auth`)

### `POST /api/auth/login`
- Public endpoint
- Request:
```json
{ "email": "user@local.test", "password": "password" }
```
- Response:
```json
{ "accessToken": "<jwt>" }
```

### `POST /api/auth/register`
- Public endpoint
- Request:
```json
{
  "email": "new@local.test",
  "displayName": "New User",
  "password": "password",
  "confirmPassword": "password"
}
```
- Response:
```json
{ "accessToken": "<jwt>" }
```

### `GET /api/auth/me`
- Requires a valid token
- Returns profile of the authenticated user

## 3) Tickets (`/api/tickets`)

### Supported statuses
`OPEN | IN_PROGRESS | ON_HOLD | RESOLVED`

### Supported priorities
`LOW | MEDIUM | HIGH`

### `POST /api/tickets`
- Roles: `USER | AGENT | ADMIN`
- Creates a ticket

### `GET /api/tickets/me`
- Roles: `USER | AGENT | ADMIN`
- Paginated list of tickets created by the authenticated user
- Query params:
  - `status` (optional)
  - `q` (optional)
  - `page` (default `0`)
  - `size` (default `20`, max `100`)

### `GET /api/tickets`
- Roles: `AGENT | ADMIN`
- Paginated list of operational queue
- Query params:
  - `scope`: `UNASSIGNED | MINE | OTHERS | ALL` (default `MINE`)
  - `status` (optional)
  - `q` (optional)
  - `page`, `size`

### `GET /api/tickets/{id}`
- Roles: `USER | AGENT | ADMIN`
- Ticket detail
- Access rules:
  - `USER`: only own tickets
  - `AGENT/ADMIN`: full access

### `PATCH /api/tickets/{id}/status`
- Roles: `AGENT | ADMIN`
- Changes ticket status

### `PATCH /api/tickets/{id}/assignment/me`
- Roles: `AGENT | ADMIN`
- Assigns ticket to authenticated user

### `POST /api/tickets/{id}/comments`
- Requires authentication
- Adds a comment to the ticket

## 4) Categories

### `GET /api/categories`
- Requires authentication
- Returns active categories

## 5) Administration (`/api/admin`)

All `/api/admin/**` routes require role `ADMIN`.

### `GET /api/admin/categories`
List categories.

### `POST /api/admin/categories`
Create category.

### `PATCH /api/admin/categories/{categoryId}`
Update category name and active flag.

### `GET /api/admin/users`
List users.

### `PATCH /api/admin/users/{userId}/active`
Activate/deactivate user.

## 6) Health

### `GET /api/health`
- Public endpoint
- Expected response: `ok`

## 7) Pagination format

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "total": 0
}
```

## 8) Evolution note

If new endpoints are added, update this file to keep a single API reference aligned with implementation.
