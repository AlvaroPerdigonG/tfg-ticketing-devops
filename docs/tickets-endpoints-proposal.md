# Tickets API - estado actual y próximos pasos

Este documento queda actualizado tras implementar los endpoints de tickets del MVP sin `slaState`.

## Endpoints implementados

### `GET /api/tickets/me`
Listado paginado de tickets creados por el usuario autenticado (`USER`, `AGENT`, `ADMIN`).

Query params:
- `status`: `OPEN|IN_PROGRESS|RESOLVED` (opcional)
- `q`: búsqueda por título (opcional)
- `page`: base 0 (default `0`)
- `size`: tamaño página (default `20`, max `100`)

### `GET /api/tickets`
Listado paginado de cola operativa para `AGENT/ADMIN`.

Query params:
- `scope`: `UNASSIGNED|MINE|OTHERS|ALL` (default `MINE`)
- `status`: `OPEN|IN_PROGRESS|RESOLVED` (opcional)
- `q`: búsqueda por título (opcional)
- `page`, `size`

Semántica de `scope`:
- `UNASSIGNED`: tickets sin agente asignado
- `MINE`: tickets asignados al agente autenticado
- `OTHERS`: tickets asignados a otros agentes
- `ALL`: todos

### `GET /api/tickets/{id}`
Detalle de ticket.
- `USER`: solo sus propios tickets (createdBy)
- `AGENT/ADMIN`: acceso completo

### Ya existentes
- `POST /api/tickets`
- `PATCH /api/tickets/{id}/status`

## Modelo de paginación usado

Se usa respuesta uniforme:

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "total": 0
}
```

Este formato es simple, estable para frontend y fácil de testear en el contexto del TFG.

## Cambios de dominio aplicados

- Añadido `priority` en `Ticket` con enum:
  - `LOW`, `MEDIUM`, `HIGH`
- Se inicializa por defecto en creación como `MEDIUM`.
- Se persiste en base de datos (`tickets.priority`).

## Próximos endpoints sugeridos (no implementados aún)

- `PATCH /api/tickets/{id}/assign`
- `PATCH /api/tickets/{id}/priority`
- `GET /api/tickets/{id}/comments`
- `POST /api/tickets/{id}/comments`

> `slaState` queda fuera por ahora para evitar complejidad innecesaria en el MVP.
