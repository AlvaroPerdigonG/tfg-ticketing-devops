# Traceability matrix

This matrix reflects the current traceability state in the repository using the chain:

**Requirement -> Scenario (`.feature`) -> Automated test evidence -> Code path**

Sources used for this snapshot:

- Functional scenarios in `docs/features/*.feature`.
- Backend scenario references via `@SpecificationRef`.
- Frontend UI scenario IDs in Vitest/RTL test titles.
- E2E scenario IDs in Playwright `test(...)` titles.
- Generated traceability report from `scripts/traceability/check-traceability.mjs`.

> Note: `.feature` files are canonical functional specifications and traceability anchors (they are not executed with Cucumber).

## Scenario status matrix (updated snapshot)

| Scenario ID | Backend coverage | Frontend UI coverage | E2E coverage | Real status | Notes (honest scope / gaps) |
|---|---|---|---|---|---|
| AUTH-01 | ✅ Unit + integration covered. | ✅ `LoginPage.test.tsx`, `AuthProvider.test.tsx`, `ProtectedRoute.test.tsx`. | ✅ `e2e/auth.spec.ts` (`AUTH-01`). | COVERED | Multi-layer evidence for login happy path (API + UI + browser). |
| AUTH-02 | ✅ Unit + integration covered. | ✅ `LoginPage.test.tsx` invalid-login flow. | ✅ `e2e/auth.spec.ts` (`AUTH-02`). | COVERED | Invalid credentials are validated across backend, UI feedback, and browser flow. |
| AUTH-03 | ✅ Unit + integration covered. | ❌ No scenario-specific UI test. | ❌ No scenario-specific E2E test. | PARTIAL | Inactive-user rejection is covered in backend only; frontend UX flow not automated. |
| AUTH-04 | ✅ Unit + integration covered. | ❌ No registration UI test. | ❌ No registration E2E test. | PARTIAL | Registration is validated at backend level; frontend registration journey lacks automation. |
| AUTH-05 | ✅ Unit + integration covered. | ❌ No duplicate-email UI test. | ❌ No duplicate-email E2E test. | PARTIAL | Duplicate email rejection currently evidenced only in backend tests. |
| TICKET-USER-01 | ✅ Unit + integration covered. | ✅ `CreateTicketPage.test.tsx` (`[TICKET-USER-01]`). | ✅ `e2e/tickets-user.spec.ts` (`TICKET-USER-01`). | COVERED | Multi-layer coverage from form submission to browser-level navigation/result. |
| TICKET-USER-02 | ✅ Integration covered. | ❌ No scenario-specific UI test (API/security-focused scenario). | ❌ No E2E scenario. | PARTIAL | Unauthorized ticket creation path is backend-only today. |
| TICKET-USER-03 | ✅ Unit + integration covered. | ⚠️ `UserTicketsHomePage.test.tsx` tagged `[TICKET-USER-03]` with fixture-level assertion. | ❌ No E2E scenario. | PARTIAL | Unit and integration backend evidence protect owner scoping; useful UI evidence exists, but full browser-level cross-user leakage proof remains absent. |
| TICKET-USER-04 | ✅ Unit + integration covered. | ❌ No ownership-specific ticket detail UI test. | ❌ No E2E scenario. | PARTIAL | Backend unit and integration evidence protect owner-only detail access; UI/E2E evidence for the journey is still missing. |
| TICKET-USER-05 | ✅ Unit + integration covered. | ✅ `UserTicketsHomePage.test.tsx` (`[TICKET-USER-05]`). | ❌ No E2E scenario. | PARTIAL | UI role restriction exists; no browser-level forbidden status-change journey yet. |
| TICKET-AGENT-01 | ✅ Unit + integration covered. | ⚠️ `AgentAdminTicketsPage.test.tsx` tagged `[TICKET-AGENT-01]` (UI action visibility). | ✅ `e2e/tickets-agent.spec.ts` (`TICKET-AGENT-01`). | COVERED | Backend + E2E demonstrate transition behavior; UI unit evidence is supportive but shallow. |
| TICKET-AGENT-02 | ✅ Unit + integration covered. | ❌ No UI invalid-transition handling test. | ❌ No E2E invalid-transition scenario. | PARTIAL | Invalid transition behavior is validated only in backend tests. |
| TICKET-AGENT-03 | ✅ Unit + integration covered. | ✅ `AgentAdminTicketsPage.test.tsx` (`[TICKET-AGENT-03]`) filter/query mapping. | ❌ No E2E scenario. | PARTIAL | Queue/filtering has backend unit/integration plus UI evidence, but no browser-level journey yet. |
| TICKET-AGENT-04 | ✅ Unit + integration covered. | ❌ No UI “assign to me” scenario test. | ✅ `e2e/tickets-agent.spec.ts` (`TICKET-AGENT-04`). | PARTIAL | Backend + E2E evidence exists; frontend UI unit scenario is still missing. |
| ADMIN-01 | ✅ Unit support + integration covered. | ❌ No scenario-specific UI test ID currently detected. | ❌ No E2E scenario. | PARTIAL | Admin user listing now has use-case unit support plus backend integration coverage; explicit scenario-tagged UI/E2E evidence remains absent. |
| ADMIN-02 | ✅ Unit support + integration covered. | ❌ No scenario-specific UI test ID currently detected. | ❌ No E2E scenario. | PARTIAL | Admin category listing now has use-case unit support plus backend integration coverage; explicit UI/E2E scenario mapping remains missing. |
| ADMIN-03 | ✅ Unit support + integration covered. | ❌ No UI deactivation scenario test. | ❌ No E2E deactivation scenario. | PARTIAL | User deactivation now has use-case unit support plus backend integration coverage; UI/E2E deactivation journeys remain missing. |
| ADMIN-04 | ✅ Integration covered. | ✅ `RequireRole.test.tsx` tagged `[ADMIN-04]`. | ✅ `e2e/admin.spec.ts` (`ADMIN-04`). | COVERED | Forbidden non-admin access is covered in backend, UI guard, and browser flow. |

## Status legend

- **NOT STARTED**: no executable test reasonably demonstrates the scenario.
- **PARTIAL**: at least one layer has evidence, but key assertions/layers are still missing.
- **COVERED**: scenario has adequate multi-layer evidence for current project goals.

## Coverage summary (current generated snapshot)

Based on `artifacts/traceability/traceability-report.md`:

- Total scenarios: **18**
- **MULTI_LAYER (covered): 5** -> AUTH-01, AUTH-02, TICKET-USER-01, TICKET-AGENT-01, ADMIN-04.
- **PARTIAL: 13**
- **NOT_REFERENCED: 0**

### Backend unit expansion note

The backend unit suite now includes additional use-case and domain-level support that is broader than the scenario-reference counters alone:

- Admin/category use-case tests for category creation/update/listing, active category listing, user listing, and user activation toggling.
- Ticket detail use-case tests for detail composition and access-rule preservation.
- Pure domain tests for ticket defaults, comments, assignment, and terminal resolved-state behavior.
- Application-owned pagination contracts (`PageQuery`, `PagedResult`, `PageDirection`) used by list use cases and test doubles instead of Spring Data `Page`/`Pageable`.

These additions improve backend diagnosis and architectural confidence while leaving API/security/persistence evidence in the backend integration layer. They do not by themselves convert scenarios to `COVERED` unless the missing UI/E2E evidence noted in the matrix is also present.

## CI/CD evaluation of the previous recommendation

The previous recommendation proposed adding traceability automation in CI. That recommendation is now **already implemented**:

- The repository includes `scripts/traceability/check-traceability.mjs`.
- CI includes a dedicated `traceability-check` job in `.github/workflows/ci.yml`.
- The job publishes Markdown/JSON artifacts each run (`artifacts/traceability/*`).

Therefore, no additional mandatory CI step is required right now for baseline traceability automation.

### Optional next step (only if stricter governance is desired)

If the team wants a stronger gate in the future, consider introducing a phase-based policy (warn -> fail) for scenarios that remain PARTIAL in prioritized epics. This is optional and should be adopted only when it does not block incremental delivery unnecessarily.
