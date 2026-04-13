# Environments and secrets management (GitHub)

## 1. Objective
Define a safe, simple, and operational approach for handling deployment environments and secrets during DevOps evolution.

## 2. Proposed environments

### `staging`
Validation environment for continuous integration:
- automatic deployment target from `main`
- basic functional verification after deploy
- early integration issue detection

### `production`
Final service environment:
- stricter controls
- approvals and release governance
- stronger smoke/operational checks

## 3. Role of each environment
- **staging**: validate integrated version outside local
- **production**: serve stable release under stricter controls

Keeping them separated avoids mixing URLs, secrets, and operational policy.

## 4. Repository secrets vs environment secrets

### Repository secrets
Use for truly global values across environments.
Example: `SONAR_TOKEN`.

### Environment secrets
Use for values that differ between staging and production, e.g.:
- `BACKEND_DEPLOY_HOOK_URL`
- `FRONTEND_DEPLOY_HOOK_URL`
- `BACKEND_BASE_URL`
- `FRONTEND_BASE_URL`

## 5. Naming convention
Keep identical key names in both environments and change only values.

Good pattern:
- `staging.BACKEND_BASE_URL = https://staging-api...`
- `production.BACKEND_BASE_URL = https://api...`

Avoid suffix proliferation (`_STAGING`, `_PROD`) unless truly necessary.

## 6. Initial secret baseline

### CI/CD base
- `SONAR_TOKEN`
- `BACKEND_DEPLOY_HOOK_URL`
- `FRONTEND_DEPLOY_HOOK_URL`
- `BACKEND_BASE_URL`
- `FRONTEND_BASE_URL`

### Optional future E2E credentials
- `E2E_USER_EMAIL` / `E2E_USER_PASSWORD`
- `E2E_AGENT_EMAIL` / `E2E_AGENT_PASSWORD`
- `E2E_ADMIN_EMAIL` / `E2E_ADMIN_PASSWORD`

Use non-personal least-privileged test accounts only.

## 7. Operational good practices
1. Never commit secrets
2. Avoid unnecessary secret duplication
3. Keep DB credentials out of GitHub if platform already manages them
4. Use GitHub Environments to isolate policies and audit trail
5. Apply least privilege
6. Rotate secrets when exposure risk exists

## 8. Manual readiness checklist
- [ ] `staging` environment exists
- [ ] `production` environment exists
- [ ] `SONAR_TOKEN` configured at agreed scope
- [ ] staging deploy/base URL secrets are present
- [ ] production deploy/base URL secrets are present
- [ ] no hardcoded sensitive values in repository
- [ ] production deployment approvals are configured
