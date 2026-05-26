# Backend cloud-ready packaging documentation

This document explains the current backend packaging model used for cloud deployment.

## 1. Cloud-ready baseline

- Runtime configuration through environment variables.
- Spring profile separation between local and cloud execution.
- Docker runtime image for the Spring Boot JAR.
- CORS configurable per environment.
- JWT key paths configurable per environment.
- No production secrets committed to the repository.

## 2. Current Docker packaging strategy

The backend currently uses a **runtime-only Dockerfile**.

File:

```text
ticketing-backend/Dockerfile
```

The image:

1. Starts from `eclipse-temurin:17-jre-jammy`.
2. Creates a non-root `spring` user.
3. Copies a prebuilt `app.jar` into `/app/app.jar`.
4. Exposes port `8080`.
5. Runs `java -jar /app/app.jar`.

This model assumes the JAR has already been built before Docker image creation.

## 3. Why the JAR is built before Docker

The production deployment workflow builds the backend JAR on GitHub Actions and sends a deployment bundle to EC2.

This avoids compiling Maven dependencies on a small EC2 instance and keeps the server-side deployment step lightweight:

- GitHub Actions compiles and packages the application.
- EC2 receives `app.jar` plus runtime files.
- Docker Compose builds the final runtime image by copying `app.jar`.

This is aligned with `.github/workflows/deploy-backend-production.yml`.

## 4. Packaging artifacts

Relevant files:

- `ticketing-backend/Dockerfile`
- `ticketing-backend/docker-compose.prod.yml`
- `ticketing-backend/Caddyfile`
- `.github/workflows/deploy-backend-production.yml`

The deployment bundle prepared by CI contains:

- `app.jar`
- `Dockerfile`
- `docker-compose.prod.yml`
- `Caddyfile`
- `.env`
- `secrets/jwt-public.pem`
- `secrets/jwt-private.pem`

## 5. Runtime profiles and configuration

Spring profile model:

- `application.yml`
  - shared defaults
  - server port from `PORT` with fallback `8080`
  - default CORS origins for local development
  - default JWT key paths for local resources
- `application-local.yml`
  - local PostgreSQL datasource
  - Flyway migrations
  - local JWT key paths
- `application-cloud.yml`
  - datasource from environment variables
  - JWT key paths from environment variables
  - proxy header support for reverse-proxy deployments

## 6. Key environment variables

### Platform/runtime

- `SPRING_PROFILES_ACTIVE=cloud`
- `PORT`

### Database

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

In the production Docker Compose file these are derived from:

- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`

### JWT security

- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_PUBLIC_KEY_LOCATION`
- `APP_SECURITY_JWT_PRIVATE_KEY_LOCATION`
- `APP_SECURITY_JWT_EXPIRATION_SECONDS` (optional)

### CORS

- `APP_SECURITY_CORS_ALLOWED_ORIGINS`

Example:

```env
APP_SECURITY_CORS_ALLOWED_ORIGINS=https://tfg-ticketing-devops.pages.dev
```

## 7. Local image build example

Because the Dockerfile expects `app.jar`, first build the JAR and copy/rename it:

```bash
cd ticketing-backend
./mvnw -B -DskipTests clean package
cp target/*.jar app.jar
docker build -t ticketing-backend:cloud-ready .
rm app.jar
```

Then run it against a local PostgreSQL instance:

```bash
docker run --rm -p 8080:8080 \
  -v "$(pwd)/src/main/resources/keys:/run/secrets:ro" \
  -e SPRING_PROFILES_ACTIVE=cloud \
  -e SPRING_DATASOURCE_URL='jdbc:postgresql://host.docker.internal:5432/ticketing' \
  -e SPRING_DATASOURCE_USERNAME='user' \
  -e SPRING_DATASOURCE_PASSWORD='password' \
  -e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_PUBLIC_KEY_LOCATION='file:/run/secrets/jwt-public.pem' \
  -e APP_SECURITY_JWT_PRIVATE_KEY_LOCATION='file:/run/secrets/jwt-private.pem' \
  -e APP_SECURITY_CORS_ALLOWED_ORIGINS='http://localhost:5173' \
  ticketing-backend:cloud-ready
```

## 8. Local compose note

For local development, prefer `ticketing-backend/docker-compose.yml`, which starts only PostgreSQL with `postgres:16-alpine`. The backend is normally run from the IDE or Maven Wrapper using the `local` profile.

## 9. Production deployment note

Production deployment is documented in:

```text
docs/devops/deploy-backend-production.md
```

That workflow deploys the backend to EC2 with Docker Compose v2, PostgreSQL and Caddy. The frontend is deployed separately through Cloudflare Pages.
