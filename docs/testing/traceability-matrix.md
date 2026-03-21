# Traceability matrix

This matrix provides the initial traceability layer for the testing strategy of the project.

The intended chain is:

**Requirement -> Scenario (`.feature`) -> Test -> Code**

The scenarios referenced below are defined in `docs/features/` and act as the canonical functional specification.

> Note: the `.feature` files are not executed with Cucumber. They are maintained as functional documentation and traceability anchors.

| Scenario ID | Feature file | Functional description | Planned test level | Existing test | Status | Notes |
|---|---|---|---|---|---|---|
| AUTH-01 | `authentication.feature` | Correct login returns access token and grants access to authenticated area. | Backend integration, Frontend UI, E2E | Backend integration exists (`AuthLoginIntegrationTest`). | Partial | Frontend login flow and E2E still pending. |
| AUTH-02 | `authentication.feature` | Invalid login is rejected. | Backend integration, Frontend UI | Backend integration exists (`AuthLoginIntegrationTest`). | Partial | Frontend error handling test is still pending. |
| AUTH-03 | `authentication.feature` | Inactive user cannot log in. | Backend unit, Backend integration | Backend unit and integration exist. | Partial | Frontend coverage is not yet planned as critical. |
| AUTH-04 | `authentication.feature` | Valid registration creates a user account and returns an access token. | Backend unit, Backend integration, Frontend UI, E2E | Backend unit and integration exist. | Partial | Frontend register flow and E2E are pending. |
| AUTH-05 | `authentication.feature` | Registration with duplicated email fails. | Backend unit, Backend integration, Frontend UI | Backend unit and integration exist. | Partial | Frontend duplicate-email behavior is pending. |
| TICKET-USER-01 | `tickets-user.feature` | Authenticated user creates a ticket successfully. | Backend unit, Backend integration, Frontend UI, E2E | Backend unit and frontend UI exist. | Partial | Backend HTTP integration and E2E are pending. |
| TICKET-USER-02 | `tickets-user.feature` | Creating a ticket without token returns `401 Unauthorized`. | Backend integration, E2E | No direct test found. | Planned | Security behavior should be covered at HTTP level. |
| TICKET-USER-03 | `tickets-user.feature` | User sees only their own tickets. | Backend integration, Frontend UI, E2E | Frontend empty-state test exists for user tickets page. | Planned | Backend listing contract is not covered yet. |
| TICKET-USER-04 | `tickets-user.feature` | User sees the detail of their own ticket. | Backend integration, Frontend UI, E2E | No direct test found. | Planned | Detail flow is a high-priority gap. |
| TICKET-USER-05 | `tickets-user.feature` | User cannot change ticket status. | Backend unit, Backend integration, Frontend UI | Backend unit exists for forbidden status change. | Partial | HTTP and frontend behavior remain pending. |
| TICKET-AGENT-01 | `tickets-agent.feature` | Agent or admin changes ticket status successfully. | Backend unit, Backend integration, Frontend UI, E2E | Backend unit exists. | Partial | Missing HTTP, frontend detail, and E2E coverage. |
| TICKET-AGENT-02 | `tickets-agent.feature` | Invalid ticket status transition returns an error. | Backend unit, Backend integration | Backend unit exists. | Partial | HTTP contract test is pending. |
| TICKET-AGENT-03 | `tickets-agent.feature` | Agent sees manageable ticket queues. | Backend integration, Frontend UI, E2E | Frontend UI test exists for URL-driven queue filters. | Partial | Backend queue listing tests are missing. |
| TICKET-AGENT-04 | `tickets-agent.feature` | Assign-to-me works correctly for agent/admin. | Backend unit, Backend integration, Frontend UI, E2E | Backend unit exists. | Partial | Missing HTTP contract, detail-page UI, and E2E. |
| ADMIN-01 | `admin.feature` | Admin lists users. | Backend integration, Frontend UI, E2E | Backend integration and frontend UI load test exist. | Partial | More detailed assertions are still needed. |
| ADMIN-02 | `admin.feature` | Admin lists categories. | Backend integration, Frontend UI, E2E | Backend integration and frontend UI load test exist. | Partial | Needs stronger behavioral coverage. |
| ADMIN-03 | `admin.feature` | Admin deactivates a user. | Backend integration, Frontend UI, E2E | Backend integration exists. | Partial | Frontend interaction and E2E remain pending. |
| ADMIN-04 | `admin.feature` | Non-admin cannot access admin endpoints. | Backend integration, Frontend UI | Backend integration exists. | Partial | Frontend route protection should also be validated. |

## Status legend

- **Planned**: scenario is defined but no relevant executable coverage exists yet.
- **Partial**: scenario has at least one relevant executable test, but coverage is incomplete across planned layers.
- **Covered**: scenario has sufficient coverage at the intended layers.

At this stage, the matrix is intentionally conservative: most scenarios are marked as **Partial** or **Planned** because the documentary strategy is being established before the next wave of implementation work.
