# Deploy backend production (EC2) with GitHub Actions

## Objective
Automate backend production deployment to AWS EC2 from GitHub Actions using SSH and Docker Compose v2, with a post-deploy smoke check over `https://.../actuator/health`.

This workflow intentionally covers **backend only** (no frontend automation in this batch).

## Deployment strategy

Current strategy uses a **build artifact model**:

- GitHub Actions compiles the Spring Boot backend with Maven.
- The deployment bundle sent to EC2 includes a prebuilt `app.jar`.
- EC2 keeps using Docker Compose, but performs only a lightweight Docker image build (`COPY app.jar`) and container restart.

This avoids Maven compilation on small EC2 instances (for example `t3.micro`), reducing CPU/RAM pressure and making SSH-based deploys more stable.

## Docker Compose requirement on EC2

Production deployment now requires **Docker Compose v2** via `docker compose` plugin.

- `docker compose` is mandatory.
- Legacy `docker-compose` v1 is no longer supported.
- The workflow runs `docker compose ... down --remove-orphans || true` before recreating the stack with `up -d --build`.

## Workflow
File: `.github/workflows/deploy-backend-production.yml`

- Name: `Deploy Backend Production`
- Triggers:
  - `workflow_run` for workflow `CI` (only after completion)
  - `workflow_dispatch`
- Concurrency: single in-flight deployment (`deploy-backend-production` group)
- Jobs (both with `environment: production`):
  1. `deploy-backend-production`
  2. `smoke-backend-production` (`needs: deploy-backend-production`)

Automatic deployment is now subordinated to CI quality gates:

- Deploy runs automatically only when `CI` concludes with `success`.
- It only auto-deploys runs where `CI` itself was triggered by `push` to `main`.
- For `workflow_run`, checkout uses `github.event.workflow_run.head_sha` so production deploys the exact commit validated by CI.
- `workflow_dispatch` is preserved for manual deployments.

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
2. Setup Java 17 (`actions/setup-java@v4`) with Maven cache.
3. Build backend jar in `ticketing-backend` with Maven Wrapper (`./mvnw -B -DskipTests clean package`).
4. Prepare a deployment bundle with runtime files only:
   - `app.jar` (copied from `ticketing-backend/target/*.jar`)
   - `docker-compose.prod.yml`
   - `Dockerfile` (runtime-only)
   - `Caddyfile`
   - `.env`
   - `secrets/jwt-public.pem`
   - `secrets/jwt-private.pem`
5. Upload `backend-production-bundle.tgz` to EC2 via SCP.

### 2) On EC2 via SSH
1. Create app directory if missing (`EC2_APP_DIR`).
2. Extract uploaded bundle.
3. Apply minimal file permissions for secrets, `.env` and `app.jar`.
4. Run remote preflight checks:
   - `docker version`
   - `docker compose version` (required)
5. If `docker compose version` is not available, deployment aborts with a clear error instructing to install Docker Compose v2 plugin on EC2.
6. Run deployment lifecycle:

```bash
docker compose -f docker-compose.prod.yml --env-file .env down --remove-orphans || true
docker compose -f docker-compose.prod.yml --env-file .env up -d --build
```

## Keepalive configuration for SSH/SCP

To improve stability during transfer and remote execution, both `scp` and `ssh` use:

- `-o ServerAliveInterval=30`
- `-o ServerAliveCountMax=10`
- `-o TCPKeepAlive=yes`

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
- No container registry yet (no `docker pull` flow in this batch).
- No frontend deployment automation in this workflow.
- No Terraform/IaC in this batch.

## Next batch candidates

- Add controlled rollback strategy.
- Add deployment notifications.
- Optionally evolve toward image registry + pull model.
