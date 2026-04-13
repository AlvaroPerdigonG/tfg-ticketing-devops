# React + TypeScript + Vite

This project starts from the standard Vite React + TypeScript template and includes additional frontend testing and E2E setup.

## Available React plugins
- `@vitejs/plugin-react` (Babel/oxc)
- `@vitejs/plugin-react-swc` (SWC)

## E2E with Playwright
A minimal E2E suite is available in `e2e/` with high-value scenarios aligned with functional IDs.

Commands:

```bash
npm run test:e2e
npm run test:e2e:headed
```

See `e2e/README.md` for environment setup details.
