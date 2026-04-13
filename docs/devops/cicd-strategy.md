# CI/CD strategy for Ticketing project

## Purpose
Define the baseline CI/CD strategy for the monorepo (`ticketing-backend` + `ticketing-frontend`).

## CI goals
Automate at least:
1. Backend unit tests
2. Backend integration tests
3. Frontend formatting/linting
4. Frontend unit/UI tests
5. Frontend production build
6. Traceability checks
7. SonarCloud analysis

## CD goals
- Automatic deploy to `staging` after successful CI on `main`
- Post-deploy smoke checks
- Production promotion as a later phase

## Quality gate principles
- Prioritize meaningful tests over percentage-only goals
- Keep lightweight but auditable traceability
- Run static analysis and basic security checks

## Tooling roles
- GitHub Actions: pipeline orchestration
- SonarCloud: code quality analysis
- Dependabot: dependency updates
- GitHub Environments: staging/production separation
