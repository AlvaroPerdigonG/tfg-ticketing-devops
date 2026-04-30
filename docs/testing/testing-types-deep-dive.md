# Testing Types Deep Dive

## 1. Introduction: testing as evidence, not as optics

This document defines how each automated test type contributes to **functional evidence** in this monorepo.

The guiding principle is explicit:

> **Test value is measured by defect prevention and confidence in business behavior, not by maximizing a numeric coverage percentage.**

The project therefore combines:

- canonical functional scenarios in `docs/features/*.feature`,
- fast unit feedback in backend and frontend,
- realistic backend integration tests with Spring + PostgreSQL,
- user-centric UI tests with React Testing Library + MSW,
- and a minimal but high-value Playwright E2E suite.

Traceability reference chain:

**Requirement -> Scenario (`.feature`) -> Automated test(s) -> Production code path**.

## 2. Testing pyramid tailored to this repository

| Test type | Main goal | Typical speed/cost | Failure diagnosis quality | Representative repository locations |
|---|---|---|---|---|
| Backend unit | Validate domain/use-case rules in isolation | Very fast / low cost | High (logic-level) | `ticketing-backend/src/test/java/.../unit/*` |
| Backend integration | Validate HTTP + security + persistence contracts in a real Spring runtime | Medium / medium-high | High (cross-layer) | `ticketing-backend/src/test/java/.../integration/*` |
| Frontend UI/component | Validate user-visible behavior at page/component level without full browser E2E | Fast-medium / medium | Medium-high (UI behavior) | `ticketing-frontend/src/**/*.test.tsx` |
| Frontend E2E | Validate critical business journeys in real browser execution | Slower / highest | High (system-level) | `ticketing-frontend/e2e/*.spec.ts` |

### Practical proportion in this project

- Keep **unit and UI/component tests** as the broad regression net.
- Use **integration tests** for backend contract confidence (especially auth, authorization, persistence).
- Keep **E2E intentionally small** and scenario-driven.

This aligns with current suite organization in backend unit tests, backend integration tests, frontend `.test.tsx`, and Playwright specs. 

## 3. Backend unit tests

### Definition

Backend unit tests validate application and domain behavior **without booting the full Spring container** and without real database I/O.

### What they validate here

- Use-case invariants and branch logic (e.g., create ticket, status transitions, assignment rules).
- Authentication and registration decision logic.
- Domain transitions and permission checks handled inside use cases.

Representative files:

- `ticketing-backend/src/test/java/com/aperdigon/ticketing_backend/unit/auth/LoginUseCaseTest.java`
- `ticketing-backend/src/test/java/com/aperdigon/ticketing_backend/unit/auth/RegisterUseCaseTest.java`
- `ticketing-backend/src/test/java/com/aperdigon/ticketing_backend/unit/ticket/ChangeTicketStatusUseCaseTest.java`
- `ticketing-backend/src/test/java/com/aperdigon/ticketing_backend/unit/ticket/AssignTicketToMeUseCaseTest.java`
- `ticketing-backend/src/test/java/com/aperdigon/ticketing_backend/unit/ticket/TicketAvailableTransitionsTest.java`

### What they do not validate

- Spring MVC serialization/deserialization.
- Security filter-chain behavior (`401` vs `403`, JWT parsing in runtime filters).
- Real JPA/PostgreSQL behavior.

### Real value

- Fast feedback for business rules.
- Low maintenance for branch-heavy logic.
- Better diagnosis granularity when a domain invariant breaks.

### Anti-patterns to avoid

- Re-testing framework internals (e.g., asserting Spring annotations from unit tests).
- Over-mocking until tests no longer represent business behavior.
- Treating unit tests as sufficient evidence for API-level correctness.

### When to write

Write backend unit tests when adding/changing:

- domain rules,
- use-case decision trees,
- error conditions,
- role/permission decisions in application services.

## 4. Backend integration tests (reference architecture)

### 4.1 Why this layer is central in this monorepo

The backend is the system of record for ticket lifecycle, authentication, and authorization. Integration tests provide strong evidence that the deployed API behaves as expected under realistic runtime conditions.

### 4.2 How they execute technically in this repository

The integration base class uses:

- `@SpringBootTest` to boot the full Spring context,
- `@AutoConfigureMockMvc` to execute HTTP requests through MVC + security stack,
- `PostgreSQLContainer("postgres:16-alpine")` for a real PostgreSQL engine,
- dynamic datasource injection via `@DynamicPropertySource`,
- persisted setup data through Spring Data repositories.

See `AbstractIntegrationTest`. It defines container startup, datasource wiring, injected `MockMvc`, and repository cleanup/helpers. 

Additional authenticated helper methods are in `AbstractAuthenticatedApiIntegrationTest`, which logs in through `/api/auth/login` and sends real Bearer tokens in request headers.

### 4.3 Explicit scope validated by these tests

A typical backend integration test in this project validates, end-to-end inside the backend runtime:

1. HTTP request mapping and payload parsing.
2. Bean validation / request constraints.
3. Security configuration and role restrictions.
4. Application service invocation.
5. Persistence adapters and database writes/reads in PostgreSQL.
6. HTTP response mapping and status code contract.

Representative files:

- `ticketing-backend/src/test/java/com/aperdigon/ticketing_backend/integration/auth/AuthApiIntegrationTest.java`
- `ticketing-backend/src/test/java/com/aperdigon/ticketing_backend/integration/ticket/CreateTicketApiIntegrationTest.java`
- `ticketing-backend/src/test/java/com/aperdigon/ticketing_backend/integration/ticket/ChangeTicketStatusApiIntegrationTest.java`
- `ticketing-backend/src/test/java/com/aperdigon/ticketing_backend/integration/admin/AdminApiIntegrationTest.java`

### 4.4 Reference example: auth integration behavior

`AuthApiIntegrationTest` demonstrates a robust integration pattern:

- Valid login returns a JWT token.
- Token claims are parsed and asserted (`sub`, `email`, `displayName`, `roles`, expiration).
- Invalid password and inactive-user flows return `401`.
- Registration path both returns token and persists user with expected role.
- Authenticated profile endpoint (`/api/auth/me`) validates token-to-profile mapping.

This is not a controller-only test; it traverses security, application services, persistence, and serialization layers.

### 4.5 What backend integration tests do **not** validate

- Full browser behavior.
- Frontend rendering/navigation.
- Reverse proxy or production infrastructure behavior.

### 4.6 Why MockMvc + Testcontainers is a good compromise

- **More realistic than mocked repository tests** due to real DB engine and full Spring runtime.
- **Faster and more deterministic than full browser/system tests** for API contracts.
- **High explanatory value in a TFG context** because each test shows clear cross-layer architecture behavior.

### 4.7 Anti-patterns to avoid

- Duplicating all happy paths in E2E when backend integration already proves the API contract.
- Turning integration tests into brittle snapshot assertions of entire JSON responses when only key contractual fields matter.
- Omitting negative/security scenarios (often higher value than duplicating trivial success cases).

### 4.8 When to write

Write backend integration tests for:

- new endpoints,
- security-sensitive changes,
- changes in persistence mapping/query behavior,
- business flows where HTTP contract is part of the requirement.

## 5. Frontend UI/component tests (Vitest + RTL + MSW)

### Definition

Frontend UI/component tests validate behavior that users perceive at page/component level, with network behavior simulated via MSW.

### Current technical execution in repository

- Test runtime: Vitest.
- Rendering/assertions: React Testing Library + `@testing-library/jest-dom`.
- API mocking: MSW server initialized in `src/test/setupTests.ts` with `onUnhandledRequest: "error"`.

The `onUnhandledRequest: "error"` policy is relevant: it prevents silent network leaks and enforces explicit contract mocking.

Representative UI tests:

- `ticketing-frontend/src/features/auth/ui/LoginPage.test.tsx`
- `ticketing-frontend/src/features/auth/ui/ProtectedRoute.test.tsx`
- `ticketing-frontend/src/features/tickets/ui/create/CreateTicketPage.test.tsx`
- `ticketing-frontend/src/features/tickets/ui/ticketsPage/UserTicketsHomePage.test.tsx`
- `ticketing-frontend/src/features/admin/ui/AdminPage.test.tsx`

### What they validate

- Visible states (loading/error/empty/data).
- Form behavior and user interaction.
- Navigation/route-guard outcomes.
- Correct API request intent (through handler assertions in tests that inspect request params/body).

### What they do not validate

- Real backend behavior.
- Browser engine specifics across full navigation flow.
- Infrastructure-level concerns (deployment, CORS in real network topology).

### Real value

- High signal for regressions in user-facing behavior.
- Faster than E2E while still checking realistic interaction patterns.
- Improves confidence in role-driven UI constraints before browser-level runs.

### Anti-patterns to avoid

- Asserting implementation details (private state, internal functions) instead of observable behavior.
- Overusing brittle text selectors when stable role/testid selectors exist.
- Mocking responses that contradict backend contract definitions.

### When to write

Write frontend UI/component tests when changing:

- page interaction logic,
- route protection logic,
- form workflows,
- role-based conditional rendering,
- error and loading UX.

## 6. End-to-end tests (Playwright)

### Definition

E2E tests validate a small set of critical end-user journeys in a real browser environment.

### Current repository scope

As documented in `ticketing-frontend/e2e/README.md`, the suite currently focuses on:

- `AUTH-01`, `AUTH-02` (`auth.spec.ts`)
- `TICKET-USER-01` (`tickets-user.spec.ts`)
- `TICKET-AGENT-01` (`tickets-agent.spec.ts`)
- `ADMIN-04` (`admin.spec.ts`)

### Execution characteristics

Playwright config defines:

- base URL from `E2E_BASE_URL` (default `http://localhost:4173`),
- retries in CI,
- traces on first retry,
- screenshots/videos on failure.

### What they validate

- Cross-page flows with real DOM/browser behavior.
- Integration of frontend routing + backend contracts in runtime.
- User role journeys where end-user confidence matters most.

### What they do not validate

- Exhaustive edge cases (that belongs mainly to unit/integration/UI layers).
- Fine-grained diagnostics when low-level logic breaks.

### Real value

- Strong product-level confidence for critical scenarios.
- High communicative value for stakeholders reviewing TFG evidence.

### Anti-patterns to avoid

- Attempting exhaustive coverage with E2E (slow and unstable).
- Using fixed sleeps instead of deterministic waits/selectors.
- Creating E2E tests for behavior already sufficiently validated at lower layers unless the user journey itself is critical.

### When to write

Add E2E tests for:

- top-priority user journeys,
- role-critical access flows,
- regressions that escaped lower layers,
- integration points where browser behavior is part of risk.

## 7. Traceability with functional scenarios

Traceability is maintained through scenario IDs (e.g., `AUTH-01`, `TICKET-AGENT-01`) in:

- `.feature` files (`docs/features/*.feature`),
- backend tests via `@SpecificationRef`,
- frontend and E2E test names.

The matrix in `docs/testing/traceability-matrix.md` should be treated as an **honest status artifact** (covered/partial/not-started), not as a compliance theatre document.

## 8. SonarCloud coverage: interpret with engineering judgment

Coverage is useful as a **heuristic signal** to reveal potentially untested code.

It is not evidence of:

- assertion quality,
- scenario completeness,
- security behavior correctness,
- architectural risk reduction.

Recommended interpretation policy:

1. First ask whether critical scenarios are evidenced across suitable layers.
2. Then inspect uncovered high-risk paths.
3. Avoid low-value tests written only to increment percentages.

## 9. Test quality criteria: strong vs superficial tests

| Criterion | Strong test signal | Superficial test signal |
|---|---|---|
| Intent clarity | Scenario/business behavior explicit | Technical noise, unclear purpose |
| Assertions | Validates outcomes with business meaning | Asserts trivial implementation details |
| Determinism | Stable setup, explicit fixtures/mocks | Flaky timing/network dependence |
| Scope discipline | Verifies one layer’s responsibilities | Mixes concerns without clear boundary |
| Defect detection power | Fails on realistic regressions | Passes despite meaningful breakage |

## 10. Roadmap (documentation + technical testing improvements)

### Documentation roadmap

- Keep `testing-strategy.md` as governance and decision document.
- Keep this deep-dive as operational guidance for contributors.
- Update traceability matrix alongside every feature-level change.

### Technical roadmap (incremental)

- Expand E2E only for highest-risk uncovered scenarios from the matrix.
- Strengthen partial frontend scenario coverage where matrix already flags shallow assertions.
- Maintain strict contract consistency between MSW fixtures and backend payload formats.

