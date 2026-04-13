# Branch policy — TFG Ticketing

## Objective
Define a strict and practical branch policy to protect code quality and traceability.

## Official branches
- `main`: protected, stable branch
- Working branches:
  - `feature/<short-description>`
  - `fix/<short-description>`
  - `chore/<short-description>`
  - `docs/<short-description>`

## Mandatory rules for `main`
1. Pull request required before merge
2. At least 1 approval
3. Dismiss stale approvals on new commits
4. Required status checks
5. No direct pushes
6. No force pushes
7. No branch deletion

## Required workflow
1. Create branch from updated `main`
2. Implement changes
3. Run local checks
4. Open PR
5. Wait for CI + review
6. Merge only when green
