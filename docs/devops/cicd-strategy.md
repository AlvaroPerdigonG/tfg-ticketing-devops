# CI/CD strategy for Ticketing project

## 1. Document purpose
Define a practical, auditable CI/CD strategy for the monorepo (`ticketing-backend` + `ticketing-frontend`) with TFG scope constraints.

## 2. CI vs CD in this project

### CI (Continuous Integration)
CI validates quality for every relevant change before merge to `main`:

- automated tests
- static checks
- build verification
- lightweight traceability checks

### CD (Continuous Delivery/Deployment)
CD automates deployment after successful CI. In this project, CD adoption is incremental:

- first stabilize CI
- then deploy automatically to staging
- finally define production promotion controls

## 3. Why testing-first before deeper DevOps
CI/CD only delivers value if test signal quality is credible. The project prioritizes:

1. stable backend and frontend tests
2. reproducible execution in CI
3. traceability evidence for TFG defense

## 4. Target CI validations
Minimum pipeline gates:

1. Backend unit tests (domain/use-case confidence)
2. Backend integration tests (HTTP, security, persistence contracts)
3. Frontend formatting/linting checks
4. Frontend unit/UI tests
5. Frontend production build
6. Traceability check for scenario IDs (`AUTH-*`, `TICKET-*`, `ADMIN-*`)
7. SonarCloud scan and report publishing

## 5. Target CD automations

### 5.1 Staging deployment from `main`
- Trigger after successful CI on merge
- Validate full deployed integration path

### 5.2 Post-deploy smoke checks
- Health endpoint checks
- basic frontend availability checks

### 5.3 Production promotion (later phase)
- explicit approval gates
- stricter operational checks

## 6. Quality-gate philosophy
- prioritize meaningful tests over vanity coverage
- keep traceability lightweight but enforceable
- use static/security analysis for early risk detection
- prefer simple, maintainable workflows over over-engineering

## 7. Branching and governance baseline
- protected `main`
- feature/fix/docs branches
- mandatory PR flow
- required checks before merge

## 8. Tool responsibilities
- **GitHub Actions**: CI/CD orchestration
- **SonarCloud**: maintainability and quality insights
- **Dependabot**: dependency update hygiene
- **Security workflow (dependency review / CodeQL when enabled)**: early security signal
- **GitHub Environments**: staging/production secret and policy isolation

## 9. Workflow structure guidance
Prefer separate workflows by responsibility:

- `ci-*` validation workflows
- `quality-*` analysis workflows
- `deploy-*` environment workflows

This improves maintainability and failure diagnosis.

## 10. Practical TFG approach
The strategy aims for:

1. **Simplicity**: operable by a single maintainer
2. **Reproducibility**: similar checks in local and CI
3. **Defensibility**: clear quality evidence for project memory/defense
