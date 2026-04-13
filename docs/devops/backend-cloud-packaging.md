# Backend cloud-ready packaging documentation

This document goes over some considerations and decisions made when preparing the application to be deployed on the cloud.

## 1. Cloud-ready baseline
- Docker image packaging
- Runtime config via environment variables
- Compatibility with PaaS platforms (Render-like model)
- CORS configurable per environment
- No production secrets hardcoded in repository

## 2. Docker packaging strategy
The backend uses a **multi-stage Dockerfile**:

1. **builder stage** (`maven:3.9.x-eclipse-temurin-17`)
   - resolves dependencies
   - compiles and packages Spring Boot JAR
2. **runtime stage** (`eclipse-temurin:17-jre-jammy`)
   - copies only the final artifact
   - runs as non-root user
   - keeps container runtime lightweight

This separates build/runtime concerns and reduces image size.

## 3. Packaging artifacts
Expected artifacts:

- `ticketing-backend/Dockerfile`
- `ticketing-backend/.dockerignore`

`.dockerignore` should exclude local artifacts (`target`, IDE metadata, logs, reports, `.git`) to avoid noisy and slow Docker builds.

## 4. Runtime profiles and configuration
Spring profile model:

- `application.yml` (shared defaults)
  - `server.port` from `PORT` with fallback
  - CORS configurable by env var
- `application-local.yml`
  - local database and local keys for development
- `application-cloud.yml`
  - datasource and key paths from env vars
  - no cloud secrets embedded in repo

## 5. Key environment variables

### Platform/runtime
- `SPRING_PROFILES_ACTIVE=cloud`
- `PORT`

### Database
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

### JWT security
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_PUBLIC_KEY_LOCATION`
- `APP_SECURITY_JWT_PRIVATE_KEY_LOCATION`
- `APP_SECURITY_JWT_EXPIRATION_SECONDS` (optional)

### CORS
- `APP_SECURITY_CORS_ALLOWED_ORIGINS`
  - CSV format
  - Example: `https://staging-frontend.example.com,https://www.example.com`

## 6. Local usage examples

### Build image
```bash
cd ticketing-backend
docker build -t ticketing-backend:cloud-ready .
```

### Run container simulating cloud
```bash
docker run --rm -p 8080:8080 \
  -v "$(pwd)/src/main/resources/keys:/run/secrets:ro" \
  -e SPRING_PROFILES_ACTIVE=cloud \
  -e PORT=8080 \
  -e SPRING_DATASOURCE_URL='jdbc:postgresql://host.docker.internal:5432/ticketing' \
  -e SPRING_DATASOURCE_USERNAME='user' \
  -e SPRING_DATASOURCE_PASSWORD='password' \
  -e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_PUBLIC_KEY_LOCATION='file:/run/secrets/jwt-public.pem' \
  -e APP_SECURITY_JWT_PRIVATE_KEY_LOCATION='file:/run/secrets/jwt-private.pem' \
  -e APP_SECURITY_CORS_ALLOWED_ORIGINS='http://localhost:5173' \
  ticketing-backend:cloud-ready
```

## 7. Local compose note
For local DB usage, keep `ticketing-backend/docker-compose.yml` healthy and aligned with configured DB user/password to avoid false-negative health checks.

## 8. Staging readiness checklist
1. Confirm cloud provider injects required env vars
2. Store JWT keys in cloud secret manager
3. Configure CORS origins for staging frontend
4. Validate DB connectivity + Flyway migrations
5. Verify `/actuator/health` after startup

This leaves the service ready for deployment workflows in later iterations.
