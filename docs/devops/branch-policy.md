# Branch policy (TFG Ticketing)

## 1. Objective
Protect repository quality and preserve traceability while keeping workflow practical for a monorepo with backend, frontend, docs, and DevOps configuration.

## 2. Scope
Applies to all contributions affecting:

- backend code
- frontend code
- infrastructure/devops config
- documentation

## 3. Official branches

### `main`
- protected stable branch
- no direct pushes
- merge only through PR

### Working branches
- `feature/<short-description>` for features
- `fix/<short-description>` for bug fixes
- `chore/<short-description>` for maintenance
- `docs/<short-description>` for documentation changes

## 4. Mandatory rules for `main`
Configure in GitHub protection/rulesets:

1. Require pull request before merging
2. Require at least one approval
3. Dismiss stale approvals on new commits
4. Require status checks to pass
5. Restrict direct pushes
6. Block force pushes
7. Block branch deletion

## 5. Required workflow
1. Create branch from updated `main`
2. Implement change in isolation
3. Run local checks
4. Push branch and open PR to `main`
5. Wait for CI + review
6. Address feedback
7. Merge when checks/approval are green

## 6. Minimum PR requirements
Each PR should include:

- change purpose
- technical scope
- validation evidence (commands/tests)
- expected impact
- known risks (if any)

## 7. Recommended merge strategy
- Prefer **Squash merge** for clean traceable history
- Enable auto-delete of branch after merge

## 8. Hotfix handling
For urgent fixes:
1. Branch from `main` using `fix/<description>`
2. Apply minimal safe correction
3. Open high-priority PR
4. Keep checks/review unless explicitly justified exception

## 9. Exceptions
Any policy bypass must be explicitly justified in PR discussion and recorded in project technical notes.
