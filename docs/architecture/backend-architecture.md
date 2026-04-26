# Backend Architecture (ticketing-backend)

## 1. Scope and goals
This document defines the backend architecture used in `ticketing-backend` and is intended to be referenced in the final thesis report.

Primary goals:
- Keep the codebase maintainable and easy to evolve in iterative deliveries.
- Enforce clear separation of concerns across layers.
- Make business rules and authorization decisions explicit and testable.
- Provide a consistent naming and packaging strategy.

---

## 2. Architectural style
The backend follows a layered architecture with clean boundaries:

**API -> Application -> Domain <- Infrastructure**

- API depends on Application.
- Application depends on Domain and on Application Ports.
- Infrastructure implements Application Ports and maps technical concerns.
- Domain does not depend on higher layers.

This is a pragmatic "clean architecture" variant adapted to a final-year engineering project.

---

## 3. Layer responsibilities

### 3.1 API layer (`api`)
Responsibilities:
- HTTP endpoints, request validation, and response mapping.
- Transport-level concerns (status codes, pagination wrappers, OpenAPI annotations).
- Delegation to use cases.

Rules:
- No direct repository orchestration in controllers.
- No business decision logic in controllers.

Key components:
- Feature controllers (`api/auth`, `api/tickets`, `api/admin`, `api/categories`).
- Shared API components (`api/shared/*`) for current user access, exception handling, and pagination.

### 3.2 Application layer (`application`)
Responsibilities:
- Use-case orchestration.
- Fine-grained authorization and business workflow coordination.
- Interaction with ports (`application/ports`).

Rules:
- Use cases are implemented as `final class` services.
- Inputs use `*Command` (write) or `*Query` (read) where applicable.
- Outputs use `*Result` records when a dedicated application response model is needed.

### 3.3 Domain layer (`domain`)
Responsibilities:
- Core business model and invariants.
- Entity behavior and state transition rules.

Rules:
- No dependency on Spring or infrastructure details.
- Domain objects remain the single source of truth for business constraints.

### 3.4 Infrastructure layer (`infrastructure`)
Responsibilities:
- Persistence adapters (JPA repositories/adapters/entities/mappers).
- Security wiring and framework configuration.
- Technical integration details.

Rules:
- Infrastructure implements application ports.
- Mapping between persistence entities and domain models is isolated in mapper classes.

---

## 4. Dependency and packaging conventions

### 4.1 Allowed dependencies
- `api -> application`
- `application -> domain`
- `application -> application.ports`
- `infrastructure -> application.ports` (+ domain mapping)
- `domain -> (none of api/application/infrastructure)`

### 4.2 Naming conventions
- Use cases: `*UseCase` (`final class`).
- Write input models: `*Command`.
- Read input models: `*Query`.
- Application output models: `*Result`.
- Ports: `*Repository` in `application/ports`.
- Infrastructure adapters: `Jpa*Repository`.
- Mapping classes: `*Mapper`.

---

## 5. Security, authentication, and authorization

### 5.1 Authentication
- JWT bearer authentication is configured through Spring Security Resource Server.
- Role claims are converted to Spring authorities.

### 5.2 Coarse-grained authorization
- Endpoint-level access control is configured in `SecurityConfig`.
- This layer decides if a request can access a route at all.

### 5.3 Fine-grained authorization
- Use cases apply contextual business authorization (ownership, role-specific operations, workflow restrictions).
- This layer decides what an authenticated actor is allowed to do in a specific business operation.

### 5.4 Current user pattern
- Controllers obtain actor identity through `CurrentUserProvider`.
- Controllers do not parse JWT claims directly for business decisions.
- Use cases consume `CurrentUser` to enforce business authorization.

---

## 6. Exception and error handling strategy

- Domain validation/business exceptions originate in `domain`.
- Application-level access/not-found exceptions are defined in `application/shared/exception`.
- API-level translation to HTTP responses is centralized in `api/shared/exception/GlobalExceptionHandler`.

This ensures consistent, centralized error contracts while preserving clean layer boundaries.

---

## 7. Tickets timeline and event model

Current event model (`TicketEventType`):
- `TICKET_CREATED`
- `STATUS_CHANGED`
- `ASSIGNED_TO_ME`

Comments are represented as timeline messages from the `Ticket` aggregate and are not modeled as a dedicated ticket event type.

Status transition availability is exposed from the domain aggregate (`Ticket.availableTransitions()`), avoiding duplicated transition logic in API DTO mapping.

---

## 8. Testing approach

- Use case behavior is primarily validated with unit tests using in-memory repositories under `test_support`.
- API behavior is covered with integration tests.
- Architectural refactors prioritize preserving endpoint contracts while moving logic to use cases.

---

## 9. Final notes for thesis defense

This architecture is intentionally pragmatic:
- It is not a full framework-heavy clean architecture implementation.
- It is consistent, testable, and realistic for an iterative academic project.
- It provides clear rationale for design decisions and maintainability trade-offs.
