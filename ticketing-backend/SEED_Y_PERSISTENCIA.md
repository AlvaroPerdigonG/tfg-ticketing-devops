# Local seed and data persistence

## Objective
Have base local data ready after startup (user, agent, category) while preserving database data across normal restarts.

## Current behavior
- `LocalSeedRunner` runs only under `local` profile.
- Seed records are inserted **only if missing**:
  - category `General`
  - user `user@local.test`
  - agent `agent@local.test`
- User existence checks are done by email (`findByEmailIgnoreCase`) to avoid collisions with unique constraints.

## Persistence between restarts
Local PostgreSQL uses a named Docker volume (`ticketing_pgdata`) in `docker-compose.yml`.
As long as the volume is not removed, data remains available.

## `run.ps1` script behavior
The script preserves DB data by default.

### Default startup (keep data)
```powershell
.\run.ps1
```

### Full reset startup
```powershell
.\run.ps1 -ResetDatabase
```

With `-ResetDatabase`, the PostgreSQL volume is removed before startup.

## Manual user creation and login
If you insert users manually, `password_hash` must contain a valid BCrypt hash for login to work.

Example SQL:

```sql
INSERT INTO users (id, email, display_name, role, is_active, password_hash)
VALUES (
  gen_random_uuid(),
  'new@local.test',
  'New User',
  'USER',
  true,
  '<bcrypt-hash>'
);
```

## Troubleshooting quick checks
1. Ensure PostgreSQL container is healthy (`docker ps`).
2. Confirm local profile is active (`SPRING_PROFILES_ACTIVE=local`).
3. Verify seed users by querying `users` table.
4. If you need a clean slate, run `-ResetDatabase`.
