# Traceability matrix

This matrix reflects the **real current state** of functional traceability in the repository with:

- canonical scenarios in `docs/features/*.feature`,
- backend traceability via `@SpecificationRef`,
- frontend UI tests (Vitest/RTL) using scenario IDs in test titles, and
- Playwright E2E tests using scenario IDs in `test(...)` titles.

Intended chain:

**Requirement -> Scenario (`.feature`) -> Test -> Code**

> Note: `.feature` files are canonical functional specification and traceability anchors (they are not executed with Cucumber).

## Scenario status matrix

| Scenario ID | Backend coverage | Frontend UI coverage | E2E coverage | Real status | Notes (honest scope / gaps) |
|---|---|---|---|---|---|
| AUTH-01 | ✅ Unit + integration covered. | ✅ `LoginPage.test.tsx`, `AuthProvider.test.tsx`, `ProtectedRoute.test.tsx`. | ✅ `e2e/auth.spec.ts` (`AUTH-01`). | COVERED | End-to-end login happy path exists and is traceable across layers. |
| AUTH-02 | ✅ Unit + integration covered. | ✅ `LoginPage.test.tsx` invalid login flow with visible error. | ✅ `e2e/auth.spec.ts` (`AUTH-02`). | COVERED | Invalid credential rejection is verified in API, UI feedback, and browser flow. |
| AUTH-03 | ✅ Unit + integration covered. | ❌ No scenario-specific UI test. | ❌ No scenario-specific E2E test. | COVERED | Scenario is currently specified at authentication backend contract/rule level; no UI/E2E implementation yet for inactive-user UX. |
| AUTH-04 | ✅ Unit + integration covered. | ❌ No registration UI test. | ❌ No registration E2E. | PARTIAL | Registration exists at backend level, but frontend registration journey is not automated yet. |
| AUTH-05 | ✅ Unit + integration covered. | ❌ No duplicate-email UI test. | ❌ No duplicate-email E2E. | PARTIAL | Duplicate email rejection is only asserted in backend tests. |
| TICKET-USER-01 | ✅ Unit + integration covered. | ✅ `CreateTicketPage.test.tsx` (`[TICKET-USER-01]`). | ✅ `e2e/tickets-user.spec.ts` (`TICKET-USER-01`). | COVERED | Ticket creation is covered from form submit to detail navigation in E2E. |
| TICKET-USER-02 | ✅ Integration covered. | ❌ No frontend UI scenario test (expected, API security scenario). | ❌ No E2E scenario. | PARTIAL | Missing browser-level unauthorized create attempt check (without token/session). |
| TICKET-USER-03 | ✅ Integration covered. | ⚠️ `UserTicketsHomePage.test.tsx` tagged `[TICKET-USER-03]`, but relies on backend response fixture and does not prove cross-user leakage by itself. | ❌ No E2E scenario. | PARTIAL | Frontend assertion is useful but partial for the full isolation claim in the scenario text. |
| TICKET-USER-04 | ✅ Integration covered. | ❌ No ticket-detail ownership UI scenario test. | ❌ No E2E scenario. | PARTIAL | Missing UI/E2E proof that owner can open and view full detail. |
| TICKET-USER-05 | ✅ Unit + integration covered. | ✅ `UserTicketsHomePage.test.tsx` tagged `[TICKET-USER-05]` (no status actions visible for user). | ❌ No E2E scenario. | PARTIAL | UI role restriction exists, but no browser E2E for forbidden status change attempt. |
| TICKET-AGENT-01 | ✅ Unit + integration covered. | ⚠️ `AgentAdminTicketsPage.test.tsx` tagged `[TICKET-AGENT-01]` only checks list-page action visibility (not transition execution). | ✅ `e2e/tickets-agent.spec.ts` (`TICKET-AGENT-01`) executes status transition and verifies result. | COVERED | Functional transition is validated in backend + E2E; frontend unit test is partial but non-blocking for scenario coverage. |
| TICKET-AGENT-02 | ✅ Unit + integration covered. | ❌ No UI test for invalid transition error handling. | ❌ No E2E invalid-transition scenario. | PARTIAL | Invalid transition is only validated in backend tests today. |
| TICKET-AGENT-03 | ✅ Integration covered. | ✅ `AgentAdminTicketsPage.test.tsx` tagged `[TICKET-AGENT-03]` + filter/query mapping assertion. | ❌ No E2E scenario. | PARTIAL | Manageable queue + filtering are in frontend unit tests, but no browser E2E for operational view. |
| TICKET-AGENT-04 | ✅ Unit + integration covered. | ❌ No UI test for “assign to me” action. | ❌ No E2E scenario. | PARTIAL | Assignment flow is currently backend-only in automated tests. |
| ADMIN-01 | ✅ Integration covered. | ⚠️ `AdminPage.test.tsx` checks tabs/data rendering, but not deep list semantics for all admin fields. | ❌ No E2E scenario. | PARTIAL | Basic UI smoke coverage exists; end-to-end admin user-list flow is missing. |
| ADMIN-02 | ✅ Integration covered. | ⚠️ `AdminPage.test.tsx` covers loaded categories at a shallow level. | ❌ No E2E scenario. | PARTIAL | Needs stronger UI assertions and/or E2E for administrative category listing behavior. |
| ADMIN-03 | ✅ Integration covered. | ❌ No UI deactivation scenario test. | ❌ No E2E deactivation scenario. | PARTIAL | User deactivation is validated only via backend automation. |
| ADMIN-04 | ✅ Integration covered. | ✅ `RequireRole.test.tsx` tagged `[ADMIN-04]` (+ route guard behavior). | ✅ `e2e/admin.spec.ts` (`ADMIN-04`). | COVERED | Forbidden access for non-admin is covered in backend, UI guard, and full browser navigation. |

## Status legend

- **NOT STARTED**: no executable test reasonably demonstrates the scenario.
- **PARTIAL**: at least one layer is covered, but key assertions/layers are still missing.
- **COVERED**: currently adequate coverage for scenario intent with existing automated tests.

## Frontend/E2E coverage summary (current snapshot)

- **Frontend UI scenarios with explicit ID in tests**: AUTH-01, AUTH-02, TICKET-USER-01, TICKET-USER-03, TICKET-USER-05, TICKET-AGENT-01, TICKET-AGENT-03, ADMIN-04.
- **E2E scenarios with explicit ID in Playwright**: AUTH-01, AUTH-02, TICKET-USER-01, TICKET-AGENT-01, ADMIN-04.
- **Scenarios with E2E still missing**: AUTH-03, AUTH-04, AUTH-05, TICKET-USER-02, TICKET-USER-03, TICKET-USER-04, TICKET-USER-05, TICKET-AGENT-02, TICKET-AGENT-03, TICKET-AGENT-04, ADMIN-01, ADMIN-02, ADMIN-03.

## Recommended next CI/CD step

Promote this matrix into a **quality gate input** in CI:

1. Add a lightweight script that parses scenario IDs from `docs/features/*.feature`.
2. Parse IDs from frontend (`*.test.tsx`) and E2E (`e2e/*.spec.ts`) titles.
3. Fail (or warn initially) when a scenario expected for the phase has no matching automated ID.
4. Publish an artifact (Markdown/JSON) each pipeline run so the matrix can be regenerated automatically for memory/DevOps deliverables.

This keeps traceability auditable and avoids manual drift between specification and test suites.
