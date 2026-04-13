# Authentication and authorization review (frontend + backend)

## Backend (current state)

### Auth endpoints
- `POST /api/auth/login`
  - Request: `{ email: string, password: string }`
  - Response: `{ accessToken: string }`
- `POST /api/auth/register`
  - Request: `{ email: string, displayName: string, password: string, confirmPassword: string }`
  - Response: `{ accessToken: string }`
- `GET /api/auth/me`
  - Requires valid JWT
  - Returns authenticated profile (`id`, `email`, `displayName`, `role`, `roles`)

### JWT security
- Signature: `RS256`
- Relevant claims:
  - `sub` (user UUID)
  - `email`
  - `displayName`
  - `roles` (role array)
  - `exp` (Unix timestamp in seconds)
- Expiration configurable via `app.security.jwt.expiration-seconds` (default 3600s)

### Effective authorization rules
- Public:
  - `POST /api/auth/**`
  - `GET /api/health` and `/actuator/health`
- Tickets:
  - `POST /api/tickets` -> `USER|AGENT|ADMIN`
  - `GET /api/tickets/me` -> `USER|AGENT|ADMIN`
  - `GET /api/tickets` -> `AGENT|ADMIN`
  - `GET /api/tickets/{id}` -> `USER|AGENT|ADMIN` (with ownership checks for `USER`)
  - `PATCH /api/tickets/{id}/status` -> `AGENT|ADMIN`
  - `PATCH /api/tickets/{id}/assignment/me` -> `AGENT|ADMIN`
  - `POST /api/tickets/{id}/comments` -> authenticated
- Administration:
  - `/api/admin/**` -> `ADMIN`
- Remaining `/api/**` -> authenticated

## Frontend (current state)

### Implemented flow
- Login/register against `/api/auth/login` and `/api/auth/register`
- Optional profile retrieval via `/api/auth/me`
- Token stored in `localStorage` as `ticketing_access_token`
- Local JWT parsing for session bootstrap (`sub`, `roles`, `exp`)
- `ProtectedRoute` redirects to `/login` for invalid sessions
- `RequireRole` redirects to `/forbidden` when role is not allowed

### Current strengths
- Frontend/backend auth contracts are aligned
- Backend authorization and frontend guards are consistent
- Stateless Bearer JWT approach fits SPA architecture

## Recommended next improvements
1. Keep OpenAPI/Swagger docs updated as API evolves
2. Evaluate refresh-token strategy for longer sessions
3. Expand E2E coverage for role-protected routes
