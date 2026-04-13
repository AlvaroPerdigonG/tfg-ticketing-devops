# Ticketing Frontend (React + TypeScript + Vite)

## Overview
This frontend is built with React, TypeScript and Vite. It includes unit/UI testing with Vitest and an E2E layer with Playwright.

## Tech stack
- React
- TypeScript
- Vite
- Vitest + React Testing Library
- Playwright (E2E)

## Local development

### Install dependencies
```bash
npm install
```

### Start dev server
```bash
npm run dev
```

### Build production bundle
```bash
npm run build
```

### Preview production build
```bash
npm run preview
```

## Quality and tests

### Lint and format checks
```bash
npm run lint
npm run format:check
```

### Unit/UI tests
```bash
npm run test:run
```

### E2E tests
```bash
npm run test:e2e
npm run test:e2e:headed
```

For E2E setup details, see `e2e/README.md`.

## Notes
- The frontend expects backend API availability and valid auth flows.
- Use stable selectors (`data-testid`) for robust E2E and UI tests.
