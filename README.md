* 📄 [TFG_PeridgonGordilloAlvaro_memoria.pdf](https://github.com/user-attachments/files/29560667/TFG_PeridgonGordilloAlvaro_memoria.pdf) - Project Thesis

* 📊 [defensa-tfg-devops.pdf](https://github.com/user-attachments/files/29560656/defensa-tfg-devops.pdf) - Project presentation slides

# TFG - Fullstack Ticketing Platform with DevOps

[![CI](https://github.com/AlvaroPerdigonG/tfg-ticketing-devops/actions/workflows/ci.yml/badge.svg)](https://github.com/AlvaroPerdigonG/tfg-ticketing-devops/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=AlvaroPerdigonG_tfg-ticketing-devops&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=AlvaroPerdigonG_tfg-ticketing-devops)
[![Deploy Backend Production](https://github.com/AlvaroPerdigonG/tfg-ticketing-devops/actions/workflows/deploy-backend-production.yml/badge.svg)](https://github.com/AlvaroPerdigonG/tfg-ticketing-devops/actions/workflows/deploy-backend-production.yml)

This repository contains the source code for the final degree project **"Design and implementation of a ticketing platform with a DevOps architecture and cloud deployment"**.

The project implements a fullstack ticketing application and uses it as a practical case study for continuous integration, continuous deployment, automated testing, quality analysis, traceability and containerized runtime environments.

## Main Technologies

Backend:
- Java 17
- Spring Boot 3.5
- Spring Security with JWT
- Spring Data JPA / Hibernate
- Flyway
- PostgreSQL
- JUnit, ArchUnit and Testcontainers

Frontend:
- React 19
- TypeScript
- Vite 7
- Ant Design
- Vitest, React Testing Library and MSW
- Playwright for manual E2E tests

DevOps and quality:
- GitHub Actions
- Docker and Docker Compose
- SonarCloud
- Dependency Review
- JaCoCo and LCOV coverage reports
- Automated traceability checks for functional scenarios

## Repository Structure

```text
.
|-- .github/workflows/              # CI, security and backend deployment workflows
|-- docs/                           # Technical, functional, testing and DevOps documentation
|   |-- devops/                     # CI/CD, deployment, branch policy and cloud packaging
|   |-- features/                   # Functional scenarios in Gherkin format
|   `-- testing/                    # Testing strategy and traceability matrix
|-- scripts/traceability/           # Traceability verification script
|-- ticketing-backend/              # Spring Boot API
|   |-- src/main/java/.../api       # REST controllers and DTOs
|   |-- src/main/java/.../application
|   |-- src/main/java/.../domain
|   |-- src/main/java/.../infrastructure
|   `-- src/main/resources          # Spring profiles, Flyway migrations and local JWT keys
`-- ticketing-frontend/             # React + Vite SPA
    |-- src/app                     # Router, layout and application bootstrap
    |-- src/features                # Auth, tickets and admin features
    |-- src/shared                  # Shared API client and utilities
    |-- src/test                    # Test setup and test utilities
    `-- e2e                         # Playwright E2E tests
```

## Local Requirements

Install:
- Git
- Docker Desktop with Docker Compose v2
- JDK 17
- Node.js compatible with Vite 7: `20.19+` or `22.12+` (Node 22 LTS is recommended)
- npm, included with Node.js

Installing Maven globally is not required because the backend includes Maven Wrapper (`mvnw` / `mvnw.cmd`).

## Local Execution

### 1. Clone the repository

```bash
git clone https://github.com/AlvaroPerdigonG/tfg-ticketing-devops.git
cd tfg-ticketing-devops
```

### 2. Start PostgreSQL

The local database is defined in `ticketing-backend/docker-compose.yml` and uses PostgreSQL 16 Alpine with a persistent Docker volume.

```bash
cd ticketing-backend
docker compose up -d
```

Local database settings:
- Host: `localhost`
- Port: `5432`
- Database: `ticketing`
- User: `user`
- Password: `password`

### 3. Run the backend

The backend must run with the Spring `local` profile.

Windows PowerShell:

```powershell
cd ticketing-backend
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Linux/macOS/Git Bash:

```bash
cd ticketing-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Windows helper script:

```powershell
cd ticketing-backend
.\run.ps1
```

To reset the local database volume:

```powershell
.\run.ps1 -ResetDatabase
```

Backend URLs:
- API: `http://localhost:8080`
- Health check: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### 4. Configure and run the frontend

Create `ticketing-frontend/.env.local`:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Install dependencies and start Vite:

```bash
cd ticketing-frontend
npm install
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

### 5. Local test users

With the `local` profile, the backend creates minimal seed data if missing:

| Role | Email | Password |
|---|---|---|
| User | `user@local.test` | `password` |
| Agent | `agent@local.test` | `password` |

The `General` category is also created automatically.

## IDE Setup

### IntelliJ IDEA for backend

1. Open `ticketing-backend` as a Maven project, or open the repository root and import the Maven module.
2. Configure the project SDK as JDK 17.
3. Enable Lombok and annotation processing if IntelliJ requests it.
4. Create a Spring Boot run configuration for `TicketingBackendApplication`.
5. Set the active profile to `local` (`SPRING_PROFILES_ACTIVE=local` or `spring-boot.run.profiles=local`).
6. Start PostgreSQL with `docker compose up -d` before running the application.

### Visual Studio Code for frontend

1. Open `ticketing-frontend`.
2. Install dependencies with `npm install`.
3. Create `.env.local` with `VITE_API_BASE_URL=http://localhost:8080`.
4. Run `npm run dev`.
5. Recommended extensions: ESLint, Prettier and TypeScript/React support.

## Tests and Validation

Backend:

```bash
cd ticketing-backend
./mvnw test
./mvnw verify -DskipITs=false
```

Frontend:

```bash
cd ticketing-frontend
npm run format:check
npm run lint
npm run test:run
npm run build
```

Manual Playwright E2E:

```bash
cd ticketing-frontend
npm run build
npm run preview
npm run test:e2e
```

The backend must be running and the frontend must point to `http://localhost:8080` through `VITE_API_BASE_URL`.

## Documentation

Main repository documentation:
- `docs/local-development.md`: detailed local execution and IDE setup guide.
- `docs/tickets-endpoints-proposal.md`: current HTTP API reference.
- `docs/auth-review.md`: authentication and authorization contract review.
- `docs/devops/cicd-strategy.md`: CI/CD strategy and quality-gate rationale.
- `docs/devops/branch-policy.md`: branch governance and pull request policy.
- `docs/devops/backend-cloud-packaging.md`: backend runtime packaging model.
- `docs/devops/aws-ec2-backend.md`: manual EC2 backend deployment architecture.
- `docs/devops/deploy-backend-production.md`: automated production backend deployment workflow.
- `docs/features/*.feature`: canonical functional scenarios used for traceability.
- `docs/testing/testing-strategy.md`: testing governance model.
- `docs/testing/testing-types-deep-dive.md`: detailed explanation of test types.
- `docs/testing/traceability-matrix.md`: current scenario coverage status.
- `ticketing-backend/SEED_Y_PERSISTENCIA.md`: local seed data and persistence notes.
- `ticketing-backend/SOLUCION_MIGRACION.md`: Flyway migration recovery notes.
- `ticketing-backend/HELP.md`: Spring Boot generated reference notes for the backend module.
- `ticketing-frontend/e2e/PLAYWRIGHT-E2E-EXECUTION.md`: manual Playwright E2E execution guide.

## CI/CD

The main workflow `.github/workflows/ci.yml` runs on pull requests and pushes to `main`. It includes:
- Backend unit tests and architecture fitness tests.
- Backend integration tests with PostgreSQL through Testcontainers.
- Frontend formatting, linting, tests and production build.
- Traceability verification.
- SonarCloud analysis with Quality Gate.

The workflow `.github/workflows/deploy-backend-production.yml` deploys the backend to production after a successful CI run on `main` or through manual dispatch. The frontend is deployed separately through Cloudflare Pages.

## License

This project is distributed under the MIT License. See `LICENSE`.

## Author

Álvaro Perdigón Gordillo  
Grau en Enginyeria Informàtica de Gestió i Sistemes d'Informació - Tecnocampus
