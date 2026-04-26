[![CI](https://github.com/AlvaroPerdigonG/tfg-ticketing-devops/actions/workflows/ci.yml/badge.svg)](https://github.com/AlvaroPerdigonG/tfg-ticketing-devops/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=AlvaroPerdigonG_tfg-ticketing-devops&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=AlvaroPerdigonG_tfg-ticketing-devops)
[![Deploy Backend Production](https://github.com/AlvaroPerdigonG/tfg-ticketing-devops/actions/workflows/deploy-backend-production.yml/badge.svg)](https://github.com/AlvaroPerdigonG/tfg-ticketing-devops/actions/workflows/deploy-backend-production.yml)
# TFG – Ticketing Platform with DevOps Architecture

This repository contains the source code for the final degree project:

**"Design and implementation of a ticketing platform with a DevOps architecture and cloud deployment"**

The project builds a full-stack ticketing application as a case study for modern software engineering practices: DevOps, CI/CD, automated quality control, and containerization.

---

## Academic context

This project is part of the Bachelor's Final Degree Project in Computer Engineering.
The repository is public for educational and applied research purposes.

---

## Main technologies

### Backend
- Java 17
- Spring Boot
- JPA / Hibernate
- PostgreSQL

### Frontend
- React
- TypeScript
- Vite

### DevOps and Quality
- GitHub Actions (CI/CD)
- Docker / Docker Compose
- SonarCloud
- ESLint / Checkstyle
- JUnit / Testcontainers
- React Testing Library

---

## Local setup and run

### Prerequisites
- Git
- Docker
- JDK 17
- Node.js LTS (20.x recommended)

### 1) Clone
```bash
git clone https://github.com/AlvaroPerdigonG/tfg-ticketing-devops.git
cd tfg-ticketing-devops
```

### 2) Start database
```bash
cd ticketing-backend
docker compose up -d
```

### 3) Run backend
```bash
cd ticketing-backend
./mvnw spring-boot:run
```
Backend URL: `http://localhost:8080`

### 4) Run frontend
```bash
cd ticketing-frontend
npm install
npm run dev
```
Frontend URL: `http://localhost:5173`

---

## OpenAPI / Swagger (local)

With backend running:
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## Project documentation

- API endpoints: `docs/tickets-endpoints-proposal.md`
- Auth review: `docs/auth-review.md`
- Testing strategy: `docs/testing/testing-strategy.md`
- Traceability matrix: `docs/testing/traceability-matrix.md`
- CI/CD strategy: `docs/devops/cicd-strategy.md`

---

## License

MIT License. See `LICENSE`.
