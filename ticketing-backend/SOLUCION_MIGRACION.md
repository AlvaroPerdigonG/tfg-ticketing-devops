# Database migration issue solution

## Problem addressed
The error below was observed during schema evolution:

`ERROR: column "password_hash" of relation "users" contains null values`

Root causes:
1. Hibernate schema update (`ddl-auto: update`) attempted unsafe changes on existing data.
2. Flyway migration flow was not consistently enforced.
3. Initial migration sequence did not fully account for existing rows.

## Solution implemented

### 1) Flyway as migration source of truth
- Add Flyway dependencies
- Ensure migrations are versioned and ordered

### 2) Safe local config
- Set Hibernate local mode to `validate` instead of `update`
- Let Flyway control schema changes

### 3) Migration scripts
- `V1__create_initial_schema.sql` (base schema)
- `V2__add_password_hash_to_users.sql` (safe password_hash evolution)
- Later migration files for ticket model evolution

## Recommended execution flow (fresh local DB)

```bash
cd ticketing-backend
docker compose down -v
docker compose up -d
./mvnw spring-boot:run
```

## Existing DB recovery path (when needed)
If DB already exists and history is inconsistent:
1. Inspect `flyway_schema_history`
2. Backup data if needed
3. Apply missing SQL manually only when justified
4. Re-align Flyway baseline strategy carefully

## Validation checklist
- App starts without schema errors
- Flyway reports successful validation/migration
- `users.password_hash` is non-null and populated
- Login works for seeded users

## Important practices
1. Do not use `ddl-auto: update` in production
2. Never rewrite applied migrations
3. Keep migrations incremental and auditable
4. Test migration paths in local/staging before production
