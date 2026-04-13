# Environments and secrets management (GitHub)

## Objective
Define a simple and secure strategy for managing environments and secrets in the DevOps phase of the project.

## Proposed environments

### `staging`
- Default destination for automatic deployments from `main`
- Used for post-deploy functional checks

### `production`
- Final service environment
- Higher controls (approvals, stricter smoke checks)

## Secret scope

### Repository secrets
Use only for repository-wide values (example: `SONAR_TOKEN`).

### Environment secrets (`staging`, `production`)
Use for environment-specific values, such as:
- `BACKEND_DEPLOY_HOOK_URL`
- `FRONTEND_DEPLOY_HOOK_URL`
- `BACKEND_BASE_URL`
- `FRONTEND_BASE_URL`

## Naming convention
Use the same key names across environments and only change the value.

## Operational best practices
1. Never commit secrets
2. Use least privilege
3. Rotate secrets when needed
4. Separate staging vs production controls

## Manual checklist
- [ ] `staging` environment exists
- [ ] `production` environment exists
- [ ] Required secrets configured in each environment
- [ ] No hardcoded sensitive data in repository
