# Database migration issue solution

## Problem summary
The error `column "password_hash" of relation "users" contains null values` happened because:
1. Hibernate was using `ddl-auto: update`
2. Flyway was not fully configured
3. Initial migrations were incomplete for existing data

## Implemented solution
1. Added Flyway dependencies
2. Changed local Hibernate mode to `validate`
3. Added SQL migrations:
   - `V1__create_initial_schema.sql`
   - `V2__add_password_hash_to_users.sql`
   - subsequent migrations for ticket evolution

## Recommended execution (fresh local DB)
1. Stop and remove DB volume (`docker compose down -v`)
2. Start DB (`docker compose up -d`)
3. Run backend in local profile

## Important notes
- Do not use `ddl-auto: update` in production
- Keep migrations incremental and immutable once applied
