# Playwright E2E Execution Guide

## Scope

This folder contains a minimal but high-value browser suite:

- `auth.spec.ts` -> `AUTH-01`, `AUTH-02`
- `tickets-user.spec.ts` -> `TICKET-USER-01`
- `tickets-agent.spec.ts` -> `TICKET-AGENT-01`
- `admin.spec.ts` -> `ADMIN-04`

The suite is intentionally focused on critical cross-page flows.

## Important execution policy in this repository

These E2E tests are **not executed automatically in the GitHub Actions CI pipeline** (`.github/workflows/ci.yml`).

Therefore, Playwright E2E scenarios must be executed **manually** when needed (for example, before release candidates, after high-risk UI routing changes, or when reproducing integration regressions).

## Local prerequisites

1. Backend running and reachable.
2. Frontend running (recommended with preview mode for deterministic E2E execution).
3. Playwright dependencies installed.

Recommended frontend startup for E2E:

```bash
cd ticketing-frontend
npm run build
npm run preview
```

Optional env vars:

```bash
export E2E_BASE_URL=http://localhost:4173
export E2E_USER_EMAIL=user@local.test
export E2E_USER_PASSWORD=password123!
export E2E_AGENT_EMAIL=agent@local.test
export E2E_AGENT_PASSWORD=password123!
```

## Execute tests manually

From `ticketing-frontend`:

### Headless

```bash
npm run test:e2e
```

### Headed

```bash
npm run test:e2e:headed
```

## Frequent setup issue

If you get `Cannot find package '@playwright/test'`, install missing pieces:

```bash
npm install -D @playwright/test
npx playwright install
```

## Debugging tips

- Debug mode: `npx playwright test --debug`
- Open report: `npx playwright show-report`

## Best practices

- Prefer stable selectors (`data-testid`).
- Avoid fixed sleeps (`waitForTimeout`).
- Assert user-visible behavior.
- Keep E2E focused on business-critical scenarios.
