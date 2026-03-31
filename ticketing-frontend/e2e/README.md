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

## Error típico: `Cannot find package '@playwright/test'`

Si al ejecutar `npm run test:e2e` o `npm run test:e2e:headed` aparece este error:

`Error [ERR_MODULE_NOT_FOUND]: Cannot find package '@playwright/test'`

significa que tienes el fichero `playwright.config.ts`, pero te falta instalar la dependencia de Playwright Test en el proyecto.

Solución:

```bash
npm install -D @playwright/test
npx playwright install
```

> `npx playwright install` descarga los navegadores que usa Playwright (Chromium, Firefox, WebKit).

---

## Mini guía: cómo añadir Playwright en un proyecto (paso a paso)

### 1) Instalar la librería de test

```bash
npm install -D @playwright/test
```

### 2) Instalar navegadores

```bash
npx playwright install
```

### 3) Crear configuración base

Crear `playwright.config.ts` y definir como mínimo:

- carpeta de tests (`testDir`),
- `baseURL` del frontend,
- proyectos/navegadores (ej. Chromium).

### 4) Crear la carpeta de tests E2E

Por convención:

- `e2e/` para la suite,
- `*.spec.ts` para cada escenario.

### 5) Añadir scripts en `package.json`

```json
{
  "scripts": {
    "test:e2e": "playwright test",
    "test:e2e:headed": "playwright test --headed"
  }
}
```

### 6) Levantar app antes de ejecutar

Playwright prueba una app real. Debes tener backend + frontend activos (o configurar `webServer` en `playwright.config.ts`).

### 7) Ejecutar y depurar

- Headless (rápido): `npm run test:e2e`
- Con navegador visible: `npm run test:e2e:headed`
- Inspector: `npx playwright test --debug`
- Reporte HTML: `npx playwright show-report`

### 8) Buenas prácticas mínimas

- Usa `data-testid` o selectores estables.
- Evita `waitForTimeout`; usa asserts con auto-wait (`expect(locator).toBeVisible()`).
- Cubre solo flujos críticos (login, crear ticket, cambio de estado, etc.).
