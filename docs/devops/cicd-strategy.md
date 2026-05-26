# CI/CD strategy for Ticketing project

## 1. Document purpose
Define a practical, auditable CI/CD strategy for the monorepo (`ticketing-backend` + `ticketing-frontend`).

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
3. traceability evidence 

## 4. Quality-gate philosophy
- prioritize meaningful tests over vanity coverage
- keep traceability lightweight but enforceable
- use static/security analysis for early risk detection
- prefer simple, maintainable workflows over over-engineering

## 5. Branching and governance baseline
- protected `main`
- feature/fix/docs branches
- mandatory PR flow
- required checks before merge

## 6. Tool responsibilities
- **GitHub Actions**: CI/CD orchestration
- **SonarCloud**: maintainability and quality insights (backend JaCoCo XML + frontend Vitest LCOV, waiting for Quality Gate result in CI)
- **Dependabot**: dependency update hygiene
- **Security workflow (dependency review / CodeQL)**: early security signal
- **GitHub Environments**: staging/production secret and policy isolation

## 7. Workflow structure guidance
Prefer separate workflows by responsibility:

- `ci-*` validation workflows
- `quality-*` analysis workflows
- `deploy-*` environment workflows

This improves maintainability and failure diagnosis.

## 8. Practical TFG approach
The strategy aims for:

1. **Simplicity**: operable by a single maintainer
2. **Reproducibility**: similar checks in local and CI
3. **Defensibility**: clear quality evidence
