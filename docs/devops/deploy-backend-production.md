# Deploy backend production (EC2) with GitHub Actions

## Objective
Automate backend production deployment to AWS EC2 from GitHub Actions using SSH and Docker Compose, with a post-deploy smoke check over `https://.../actuator/health`.

This workflow intentionally covers **backend only** (no frontend automation in this batch).

## Workflow
File: `.github/workflows/deploy-backend-production.yml`

- Name: `Deploy Backend Production`
- Triggers:
  - `push` on `main`
  - `workflow_dispatch`
- Concurrency: single in-flight deployment (`deploy-backend-production` group)
- Jobs (both with `environment: production`):
  1. `deploy-backend-production`
  2. `smoke-backend-production` (`needs: deploy-backend-production`)

## Required `production` environment secrets

- `EC2_HOST`
- `EC2_USER`
- `EC2_SSH_PRIVATE_KEY`
- `EC2_SSH_KNOWN_HOSTS`
- `EC2_APP_DIR`
- `EC2_SSH_PORT` (optional, defaults to `22`)
- `BACKEND_BASE_URL`
- `DOMAIN`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `APP_SECURITY_CORS_ALLOWED_ORIGINS`
- `JWT_PUBLIC_KEY_PEM`
- `JWT_PRIVATE_KEY_PEM`

## Deployment sequence

### 1) On GitHub runner
1. Checkout repository.
2. Prepare a deployment bundle with minimal backend files:
   - `docker-compose.prod.yml`
   - `Dockerfile`
   - `Caddyfile`
   - Maven wrapper + `.mvn`
   - `pom.xml`
   - `src/`
3. Generate `.env` from `production` secrets.
4. Generate JWT key files:
   - `secrets/jwt-public.pem`
   - `secrets/jwt-private.pem`
5. Upload `backend-production-bundle.tgz` to EC2 via SCP.

### 2) On EC2 via SSH
1. Create app directory if missing (`EC2_APP_DIR`).
2. Extract uploaded bundle.
3. Apply minimal file permissions for secrets.
4. Detect compose command:
   - try `docker compose`
   - fallback to `docker-compose`
5. Run deployment:

```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

(or same with `docker-compose` fallback).

## `.env` and JWT key handling

The workflow generates `.env` with fixed cloud profile + key paths:

- `SPRING_PROFILES_ACTIVE=cloud`
- `DOMAIN`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `APP_SECURITY_CORS_ALLOWED_ORIGINS`
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_PUBLIC_KEY_LOCATION=file:/run/secrets/jwt-public.pem`
- `APP_SECURITY_JWT_PRIVATE_KEY_LOCATION=file:/run/secrets/jwt-private.pem`

JWT PEM content is sourced from secrets (`JWT_PUBLIC_KEY_PEM`, `JWT_PRIVATE_KEY_PEM`) and written to files on each deployment.

## JWT secrets mount in compose

`ticketing-backend/docker-compose.prod.yml` mounts local `./secrets` into backend container as read-only:

- `./secrets:/run/secrets:ro`

This keeps the production behavior versioned and aligned with the manual EC2 setup validated previously.

## Smoke check

`smoke-backend-production` job polls:

- `${BACKEND_BASE_URL}/actuator/health`

using `curl` + retries and requires JSON response with status `UP`.

## Current limitations (intentional)

- No automated rollback yet.
- No container registry yet (build occurs on EC2 via compose).
- No frontend deployment automation in this workflow.
- No Terraform/IaC in this batch.

## Next batch candidates

- Add controlled rollback strategy.
- Add deployment notifications.
- Optionally evolve toward image registry + pull model.
