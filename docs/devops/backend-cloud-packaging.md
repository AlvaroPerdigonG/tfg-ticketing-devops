# Backend cloud-ready packaging

## Objective
Prepare `ticketing-backend` for cloud deployment with Docker packaging and runtime configuration by environment variables.

## Docker strategy
Multi-stage Dockerfile:
1. **builder**: compiles and packages Spring Boot JAR
2. **runtime**: runs lightweight image as non-root user

## Runtime profiles
- `application.yml`: shared defaults
- `application-local.yml`: local development
- `application-cloud.yml`: cloud runtime settings

## Key environment variables
- `SPRING_PROFILES_ACTIVE`
- `PORT`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_PUBLIC_KEY_LOCATION`
- `APP_SECURITY_JWT_PRIVATE_KEY_LOCATION`
- `APP_SECURITY_CORS_ALLOWED_ORIGINS`

## Local image usage example
```bash
cd ticketing-backend
docker build -t ticketing-backend:cloud-ready .
```

## Pre-deploy checklist
1. Confirm cloud platform injects required env vars
2. Store JWT keys in platform secret manager
3. Configure CORS with real frontend domains
4. Validate DB connectivity and Flyway migrations
