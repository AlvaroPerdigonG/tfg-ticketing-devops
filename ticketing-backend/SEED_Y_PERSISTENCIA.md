# Local seed and data persistence

## Objective
Provide base local data at startup while keeping database data persistent across restarts.

## Current behavior
- `LocalSeedRunner` runs only in `local` profile
- Inserts base data only if missing:
  - category `General`
  - user `user@local.test`
  - agent `agent@local.test`
- User lookup is by email (`findByEmailIgnoreCase`)

## Persistence across restarts
`docker-compose.yml` uses a named PostgreSQL volume (`ticketing_pgdata`).
If you do not remove the volume, data remains available.

## `run.ps1` behavior
- Default run keeps DB data
- `-ResetDatabase` removes the PostgreSQL volume and starts fresh

## Manual users and login
A manually inserted user must have a BCrypt password hash in `password_hash` to login.
