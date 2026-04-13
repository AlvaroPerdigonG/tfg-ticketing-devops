# E2E suite with Playwright

## Scope

This folder contains a minimal but high-value browser suite:

- `auth.spec.ts` -> `AUTH-01`, `AUTH-02`
- `tickets-user.spec.ts` -> `TICKET-USER-01`
- `tickets-agent.spec.ts` -> `TICKET-AGENT-01`
- `admin.spec.ts` -> `ADMIN-04`

The suite is intentionally focused on critical cross-page flows.

## Local prerequisites

1. Backend running and reachable
2. Frontend running (usually via `npm run preview`)
3. Playwright dependencies installed

Optional env vars:

```bash
export E2E_BASE_URL=http://localhost:4173
export E2E_USER_EMAIL=user@local.test
export E2E_USER_PASSWORD=password123!
export E2E_AGENT_EMAIL=agent@local.test
export E2E_AGENT_PASSWORD=password123!
```

## Execute tests

### Headless

```bash
npm run test:e2e
```

### Headed

```bash
npm run test:e2e:headed
```

## Frequent setup issue

If you get:

`Cannot find package '@playwright/test'`

install missing pieces:

```bash
npm install -D @playwright/test
npx playwright install
```

## Debugging tips

- Debug mode: `npx playwright test --debug`
- Open report: `npx playwright show-report`

## Best practices

- Prefer stable selectors (`data-testid`)
- Avoid fixed sleeps (`waitForTimeout`)
- Assert user-visible behavior
- Keep E2E focused on business-critical scenarios
