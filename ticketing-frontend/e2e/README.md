# Minimal E2E suite with Playwright

This folder contains a small high-value E2E suite:

- `auth.spec.ts` -> `AUTH-01`, `AUTH-02`
- `tickets-user.spec.ts` -> `TICKET-USER-01`
- `tickets-agent.spec.ts` -> `TICKET-AGENT-01`
- `admin.spec.ts` -> `ADMIN-04`

## Local requirements
1. Backend running and reachable
2. Frontend running (default: `http://localhost:4173` with `npm run preview`)
3. Optional E2E env vars:

```bash
export E2E_BASE_URL=http://localhost:4173
export E2E_USER_EMAIL=user@local.test
export E2E_USER_PASSWORD=password123!
export E2E_AGENT_EMAIL=agent@local.test
export E2E_AGENT_PASSWORD=password123!
```

## Run
```bash
npm run test:e2e
```

Headed mode:
```bash
npm run test:e2e:headed
```

## Common error: `Cannot find package '@playwright/test'`
Install dependencies and browsers:

```bash
npm install -D @playwright/test
npx playwright install
```

## Good practices
- Use stable selectors (`data-testid`)
- Avoid fixed sleeps
- Focus on critical user journeys
