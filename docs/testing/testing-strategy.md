# Testing strategy

## Purpose

This document defines the testing strategy for the TFG ticketing platform.

The project aims to combine:

- functional correctness,
- maintainable automated testing,
- lightweight traceability,
- and CI/CD-friendly execution.

The reference chain for the project is:

**Requirement -> Scenario (`.feature`) -> Test -> Code**

The goal is not to treat tests as isolated technical artifacts, but as verifiable evidence that each relevant functional scenario has a clear specification and at least one automated validation path.

---

## Canonical functional specification

The files under `docs/features/` are the **canonical functional specification** for the most relevant business scenarios of the platform.

These `.feature` files are used as:

- a stable business-readable specification,
- a source for scenario identifiers,
- and a traceability anchor between requirements, executable tests, and code.

### Important clarification

These `.feature` files are **not executed with Cucumber**.

The project deliberately avoids Cucumber and similar BDD runners. The executable tests will continue to be implemented with the project testing stack:

- **JUnit** for backend unit and integration tests,
- **Vitest + React Testing Library** for frontend unit and UI/component tests,
- **Playwright** for end-to-end scenarios.

Therefore, the `.feature` files are specifications, not test runners.

---

## Test pyramid of the project

The testing model follows a pragmatic test pyramid adapted to a SaaS ticketing platform.

### 1. Unit tests

Unit tests should be the most numerous and the fastest.

Their purpose is to validate isolated business rules and application services such as:

- authentication use cases,
- registration rules,
- ticket creation rules,
- status transition rules,
- assignment rules,
- authorization decisions contained in application services,
- and domain invariants.

Typical technologies:

- Backend: **JUnit**
- Frontend: **Vitest** for isolated logic where applicable

### 2. Integration tests

Integration tests validate the behavior of relevant subsystems working together.

In this project, this mainly means:

- Spring Boot controllers,
- security filters,
- persistence adapters,
- database interaction,
- serialization/deserialization,
- and contract behavior of HTTP endpoints.

Typical technologies:

- Backend: **JUnit + Spring Boot Test + MockMvc + Testcontainers**

These tests are especially important for the TFG because they provide evidence that the backend API, security rules, and persistence layer behave correctly under realistic conditions.

### 3. UI/component tests

UI/component tests validate the behavior of frontend pages and components in isolation from full browser E2E execution.

They should cover:

- rendering of relevant states,
- role-based conditional UI,
- form validation and submission behavior,
- navigation decisions,
- empty/loading/error states,
- and interaction with mocked API clients.

Typical technologies:

- Frontend: **Vitest + React Testing Library**

These tests may be co-localized next to pages and components when that improves maintainability.

### 4. End-to-end tests

E2E tests validate the most critical user journeys across the whole system.

They should be fewer in number, but highly valuable.

Typical examples for this project are:

- login,
- registration,
- user creates ticket,
- agent assigns ticket,
- agent changes ticket status,
- admin manages users or categories.

Typical technologies:

- **Playwright**

---

## What is tested in the backend

The backend test suite should focus on the following layers.

### Backend unit scope

Backend unit tests should validate:

- use cases,
- domain rules,
- permission rules enforced in application services,
- error conditions,
- and state transitions.

Examples:

- valid and invalid login,
- successful and unsuccessful registration,
- ticket creation with valid and invalid input,
- ticket assignment rules,
- ticket status changes,
- and access restrictions for non-authorized actors.

### Backend integration scope

Backend integration tests should validate:

- HTTP contracts,
- security behavior (`401`, `403`, permitted endpoints, role restrictions),
- request validation,
- persistence with PostgreSQL,
- JWT-based authentication behavior,
- and end-to-end backend flows inside the Spring application.

Examples:

- `/api/auth/login`,
- `/api/auth/register`,
- `/api/auth/me`,
- `/api/tickets`,
- `/api/tickets/me`,
- `/api/tickets/{id}`,
- `/api/tickets/{id}/status`,
- `/api/tickets/{id}/assignment/me`,
- `/api/admin/users`,
- `/api/admin/categories`.

The backend is the system of record for business rules, so integration coverage is especially important for demonstrating robustness in the TFG.

---

## What is tested in the frontend

The frontend test suite should focus on user-visible behavior rather than implementation details.

### Frontend unit/UI scope

Frontend tests should validate:

- page rendering,
- protected navigation,
- role-based route decisions,
- forms such as login, registration, and create ticket,
- list views,
- detail views,
- admin screens,
- and visible error/loading/empty states.

Where possible, tests should assert:

- what the user sees,
- what the user can do,
- and what API interaction is triggered.

### Frontend E2E scope

Playwright should validate the most relevant cross-page workflows with real browser execution.

The E2E suite should remain intentionally small and should focus on canonical business scenarios rather than exhaustive UI permutations.

---

## Why Cucumber is not used

The project uses Gherkin-style `.feature` files as documentation, but it does **not** use Cucumber.

This decision is intentional.

### Reasons

1. **Lower tooling overhead**  
   Cucumber introduces an additional execution layer, step-definition maintenance, and glue code that can become expensive in a small-to-medium academic project.

2. **Reduced duplication**  
   With Cucumber, there is often duplication between scenario text, step definitions, and actual test logic. This project prefers writing executable tests directly in JUnit, Vitest, and Playwright.

3. **Better alignment with the existing stack**  
   The project already uses Java/Spring Boot and React/Vitest. Directly using their native testing tools keeps the suite simpler and easier to maintain.

4. **Clearer ownership of test intent**  
   The `.feature` file defines the functional contract, while the executable test is written in the most suitable technical layer.

5. **Better fit for CI/CD**  
   Native test tools are easier to run, parallelize, and report in standard pipelines.

In short, the project adopts the **discipline of BDD-style specification** without adopting the **runtime layer of Cucumber**.

---

## Why lightweight traceability is used

The project uses **lightweight traceability** rather than a heavy requirements management process.

### What lightweight traceability means here

Each important functional scenario gets:

- a stable scenario identifier,
- a canonical scenario description in a `.feature` file,
- one or more planned executable tests,
- and a mapping entry in the traceability matrix.

### Why this is the right level for the project

1. It is academically defensible for a TFG.
2. It keeps functional intent visible.
3. It prevents the test suite from becoming disconnected from requirements.
4. It avoids excessive process overhead.
5. It makes progress measurable over time.

The objective is not bureaucratic traceability; the objective is **practical and auditable linkage** between what the system must do and what is actually tested.

---

## Relationship between scenarios and executable tests

A scenario in `docs/features/` does not imply a single test file.

A single scenario may be validated by several test layers, for example:

- a backend unit test for business rules,
- a backend integration test for the HTTP contract,
- a frontend UI/component test for page behavior,
- and optionally a Playwright E2E for the full journey.

This is acceptable and desirable.

The key rule is that the scenario identifier must remain stable and must be easy to reference from tests, planning documents, PRs, or CI reporting.

---

## How this strategy fits CI/CD

This testing strategy is designed to integrate naturally with CI/CD.

### Expected pipeline structure

A typical pipeline should evolve toward these stages:

1. **Static checks**  
   Formatting, linting, and basic quality gates.

2. **Backend unit tests**  
   Fast feedback on domain and use case rules.

3. **Frontend unit/UI tests**  
   Fast feedback on pages, forms, and role-based UI behavior.

4. **Backend integration tests**  
   Validation of HTTP contracts, security, and persistence.

5. **E2E smoke scenarios**  
   Validation of a small number of critical business flows.

6. **Coverage and reporting artifacts**  
   Coverage reports, test summaries, and traceability status where feasible.

### Value for CI/CD

This model helps the project achieve:

- faster feedback at lower layers,
- stronger confidence before deployment,
- clearer evidence for academic evaluation,
- and a direct link between requirements and automation.

For a DevOps-oriented TFG, the important point is that testing is not an isolated activity: it becomes part of the delivery pipeline and release confidence model.

---

## Governance and maintenance rules

To keep this strategy stable over time, the project should follow these rules.

1. Every new critical feature should add or update at least one scenario in `docs/features/`.
2. Each scenario must have a stable ID.
3. The traceability matrix must be updated when tests are added or removed.
4. Executable tests should reference scenario IDs whenever practical.
5. The `.feature` files should remain readable by humans and stable over time.
6. The `.feature` files should avoid technical implementation details.
7. The executable tests remain the source of automated verification, while the `.feature` files remain the source of functional intent.

---

## Summary

This project adopts a layered testing strategy with lightweight traceability.

Its key principles are:

- canonical functional scenarios in `.feature` files,
- executable tests implemented with native tools rather than Cucumber,
- a clear chain from requirement to code,
- and CI/CD-compatible automation by layers.

This approach is well suited to the objectives of the TFG: serious testing, good engineering practices, and a defensible DevOps-oriented quality strategy.
