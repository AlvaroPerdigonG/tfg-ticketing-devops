# Propuesta de endpoints para páginas de Tickets (USER / AGENT / ADMIN)

> Objetivo: habilitar completamente las páginas implementadas en frontend con filtros, paginación y acciones operativas.

## 1) Listado de tickets del usuario autenticado (USER)

### `GET /api/tickets/me`
Lista los tickets creados por el usuario logado.

**Query params (opcionales)**
- `status`: `OPEN|IN_PROGRESS|RESOLVED`
- `q`: texto libre (id/título)
- `page`: número de página (base 0)
- `size`: tamaño de página
- `sort`: ejemplo `createdAt,desc`

**200 Response**
```json
{
  "items": [
    {
      "id": "uuid",
      "title": "No puedo acceder",
      "status": "OPEN",
      "priority": "MEDIUM",
      "createdAt": "2026-01-02T10:15:30Z",
      "updatedAt": "2026-01-02T10:16:12Z",
      "category": { "id": "uuid", "name": "Accesos" }
    }
  ],
  "page": 0,
  "size": 20,
  "total": 148
}
```

---

## 2) Cola operativa para AGENT/ADMIN

### `GET /api/tickets`
Listado global para soporte con filtros avanzados.

**Query params (opcionales)**
- `scope`: `UNASSIGNED|MINE|ALL`  
- `status`: lista CSV (`OPEN,IN_PROGRESS`)
- `priority`: lista CSV (`LOW,MEDIUM,HIGH,URGENT`)
- `categoryId`: UUID
- `createdFrom`, `createdTo`: ISO datetime
- `updatedFrom`, `updatedTo`: ISO datetime
- `slaState`: `ON_TRACK|AT_RISK|BREACHED`
- `assignedTo`: UUID (solo ADMIN)
- `q`: texto libre
- `page`, `size`, `sort`

**200 Response**
```json
{
  "items": [
    {
      "id": "uuid",
      "title": "Error en facturación",
      "status": "IN_PROGRESS",
      "priority": "HIGH",
      "createdAt": "2026-01-02T10:15:30Z",
      "updatedAt": "2026-01-02T11:45:00Z",
      "createdBy": { "id": "uuid", "displayName": "Juan" },
      "assignedTo": { "id": "uuid", "displayName": "Agente 1" },
      "category": { "id": "uuid", "name": "Facturación" },
      "slaState": "AT_RISK"
    }
  ],
  "page": 0,
  "size": 20,
  "total": 523
}
```

---

## 3) Métricas para cards de dashboard/tickets operativos

### `GET /api/tickets/metrics`

**Query params (opcionales)**
- `scope`: `MINE|TEAM|ALL`
- `categoryId`

**200 Response**
```json
{
  "unassigned": 24,
  "assignedToMe": 17,
  "inProgress": 31,
  "awaitingCustomer": 9,
  "slaAtRisk": 12,
  "slaBreached": 3
}
```

---

## 4) Detalle de ticket

### `GET /api/tickets/{id}`

**200 Response**
```json
{
  "id": "uuid",
  "title": "No puedo acceder",
  "description": "...",
  "status": "OPEN",
  "priority": "MEDIUM",
  "createdAt": "2026-01-02T10:15:30Z",
  "updatedAt": "2026-01-02T10:16:12Z",
  "createdBy": { "id": "uuid", "displayName": "Juan" },
  "assignedTo": { "id": "uuid", "displayName": "Agente 1" },
  "category": { "id": "uuid", "name": "Accesos" }
}
```

---

## 5) Asignación y acciones operativas

### `PATCH /api/tickets/{id}/assign`
Body:
```json
{ "assignedToUserId": "uuid" }
```

### `PATCH /api/tickets/{id}/priority`
Body:
```json
{ "priority": "LOW|MEDIUM|HIGH|URGENT" }
```

> Nota: el backend ya tiene `PATCH /api/tickets/{id}/status`; este endpoint encaja con esta propuesta.

---

## 6) Comentarios (detalle / colaboración)

### `GET /api/tickets/{id}/comments`
### `POST /api/tickets/{id}/comments`
Body:
```json
{ "body": "mensaje" }
```

---

## 7) Contratos recomendados

- Añadir `priority` y `slaState` al modelo de ticket para la vista AGENT/ADMIN.
- Respuesta paginada uniforme (`items/page/size/total`) para listados.
- Filtros por query params, evitando endpoints duplicados por cada vista.
