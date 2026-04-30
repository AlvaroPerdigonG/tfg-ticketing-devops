# Testing strategy

## Purpose

This document defines the testing governance model for the TFG ticketing platform monorepo.

The strategy is intentionally **value-oriented**:

- prioritize defect prevention and behavioral confidence,
- keep tests maintainable and CI-friendly,
- preserve end-to-end traceability from requirement to automated evidence,
- and avoid "coverage inflation" through low-value assertions.

Reference chain used across the repository:

**Requirement -> Scenario (`.feature`) -> Automated test(s) -> Production code path**.

For detailed operational guidance per test type, see:

- `docs/testing/testing-types-deep-dive.md`
- `docs/testing/traceability-matrix.md`

---

## Canonical functional specification

The files under `docs/features/` are the canonical functional specification for the most relevant product scenarios.

They provide:

- business-readable scenario definitions,
- stable scenario identifiers (e.g., `AUTH-01`, `TICKET-AGENT-01`),
- and traceability anchors for backend, frontend, and E2E tests.

### Clarification: `.feature` files are not executable in this repository

The project deliberately does **not** execute these files with Cucumber.

Executable tests are implemented directly with the project stack:

- **JUnit** (backend unit + backend integration),
- **Vitest + React Testing Library** (frontend UI/component),
- **Playwright** (frontend E2E).

This keeps tooling lean and reduces glue-code overhead while preserving BDD-style specification quality.

---

## Testing pyramid applied to this monorepo

### 1) Unit tests (backend-first for business rules)

Unit tests are expected to be the fastest and most numerous.

Primary role:

- protect domain/application logic (use-case rules, transitions, validations),
- provide fast feedback for regressions in business decisions,
- isolate logic defects with high diagnosis precision.

### 2) Backend integration tests

Integration tests are central to this project because the backend is the system of record.

Primary role:

- validate HTTP contracts,
- verify runtime security behavior (authentication/authorization),
- assert persistence behavior against PostgreSQL,
- and confirm cross-layer API behavior under real Spring runtime.

### 3) Frontend UI/component tests

UI/component tests validate user-visible behavior of pages/components using deterministic API mocking.

Primary role:

- assert rendering, interaction, and route-guard behavior,
- verify loading/error/empty/data states,
- and catch regressions early without full browser cost.

### 4) End-to-end tests (Playwright)

E2E tests are intentionally fewer and focused on critical cross-page business journeys.

Primary role:

- provide system-level confidence for high-value workflows,
- and validate real-browser behavior that lower layers cannot prove alone.

---

## Backend integration reference architecture (explicit scope)

Backend integration tests in this repository execute with:

- full Spring context (`@SpringBootTest`),
- real MVC request pipeline (`MockMvc`),
- real PostgreSQL via Testcontainers,
- runtime JWT authentication and role checks,
- repository-backed persistence and HTTP response assertions.

This scope is implemented in:

- `ticketing-backend/src/test/java/com/aperdigon/ticketing_backend/test_support/integration/AbstractIntegrationTest.java`
- `ticketing-backend/src/test/java/com/aperdigon/ticketing_backend/test_support/integration/AbstractAuthenticatedApiIntegrationTest.java`

### What this layer validates

- controller mapping and request validation,
- security filters and authorization outcomes (`401`/`403`/allowed),
- application-service orchestration,
- JPA/repository persistence behavior,
- endpoint contract responses.

### What this layer does not validate

- browser rendering/navigation,
- production infrastructure specifics (edge proxy behavior, CDN/network topology).

---

## Coverage policy (SonarCloud and CI)

Coverage is treated as a **diagnostic metric**, not a standalone quality objective.

Policy:

1. Use coverage to identify suspiciously untested code paths.
2. Prioritize tests by business risk and scenario criticality.
3. Reject low-value tests whose only purpose is to raise percentage.
4. Assess quality through both coverage and traceability evidence.

For this TFG, the strongest quality evidence is:

- scenario traceability,
- meaningful assertions,
- and coherent multi-layer validation where risk justifies it.

---

## Traceability and governance

The traceability matrix (`docs/testing/traceability-matrix.md`) is the living status artifact that records:

- covered vs partial vs missing scenarios,
- layer distribution of evidence,
- and honest gaps to prioritize next.

The matrix is intentionally transparent: partial coverage remains explicitly marked partial.

---

## Decision rules for contributors

When adding or modifying behavior:

1. Identify affected scenario(s) in `docs/features`.
2. Select the lowest-cost layer that can provide credible evidence.
3. Add higher-layer tests only when lower layers cannot prove the risk.
4. Keep assertions behavior-centric and deterministic.
5. Update traceability status honestly.

---

## Document map

- Strategy/governance: `docs/testing/testing-strategy.md` (this file)
- Layer-by-layer deep explanation: `docs/testing/testing-types-deep-dive.md`
- Current scenario coverage status: `docs/testing/traceability-matrix.md`

