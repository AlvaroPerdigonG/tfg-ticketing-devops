# E2E mínimo con Playwright

Esta carpeta contiene una suite E2E intencionalmente pequeña y orientada a valor:

- `auth.spec.ts` → `AUTH-01`, `AUTH-02`
- `tickets-user.spec.ts` → `TICKET-USER-01`
- `tickets-agent.spec.ts` → `TICKET-AGENT-01`
- `admin.spec.ts` → `ADMIN-04`

## Requisitos locales

1. Backend levantado y accesible.
2. Frontend ejecutándose (por defecto en `http://127.0.0.1:4173` con `npm run preview`).
3. Variables de entorno E2E (opcionales):

```bash
export E2E_BASE_URL=http://127.0.0.1:4173
export E2E_USER_EMAIL=user@local.test
export E2E_USER_PASSWORD=password
export E2E_AGENT_EMAIL=agent@local.test
export E2E_AGENT_PASSWORD=password
```

## Ejecución

```bash
npm run test:e2e
```

Modo con navegador visible:

```bash
npm run test:e2e:headed
```
