# Git & GitHub Workflow Standards

## Project: Expense Tracker
### AI-Powered Personal Expense Tracking, Forecasting and Financial Recommendation System

**Document Version:** 1.0.0  
**Status:** Approved  
**Date:** November 2026  

---

## 1. Remote Repository & Branching Strategy

- **Official Remote URL:** `https://github.com/purush777196/expense-tracker.git`
- **Default Branch:** `main` (Production-ready, stable codebase)
- **Feature Branches:** All ongoing development occurs on descriptive feature branches cut from `main`.

### Standard Branch Naming Conventions:
- `feature/backend-foundation`: Spring Boot, Flyway, and JWT authentication.
- `feature/dashboard`: React layouts, routing, and dashboard widgets.
- `feature/transaction-management`: Ledger CRUD, filtering, and pagination.
- `feature/ai-categorization`: NLP model training and FastAPI inference.
- `feature/expense-forecasting`: Time-series XGBoost forecasting.
- `feature/budget-management`: Budget calculations and overrun alarms.
- `feature/anomaly-detection`: Isolation forest outlier scoring.
- `feature/financial-recommendations`: Rule-based recommendation engine.
- `feature/devops-deployment`: Docker, AWS configurations, and CI/CD pipelines.
- `bugfix/<issue-description>`: Targeted defect remediations.
- `docs/<documentation-update>`: Standalone documentation improvements.

---

## 2. Conventional Commit Standards

Every commit must follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <description in imperative mood>

[optional body providing technical context]

[optional footer(s) referencing issue/PR]
```

### Commit Types:
- `feat`: A new user-facing or system capability.
- `fix`: A bug fix or defect correction.
- `docs`: Documentation updates only.
- `style`: Formatting, missing semicolons, whitespace (no functional code changes).
- `refactor`: Code reorganization without behavior change.
- `perf`: Code modification specifically aimed at improving performance.
- `test`: Adding missing tests or correcting existing tests.
- `build`: Changes affecting build systems, Maven POMs, npm packages, or dependencies.
- `ci`: Changes to CI/CD workflows and automated scripts.
- `chore`: Maintenance tasks, `.gitignore` updates, configuration changes.

### Examples:
```bash
feat(auth): implement BCrypt password hashing and JWT token generator
feat(tx): add composite index and pagination support to transaction query
fix(budget): correct month-end overrun calculation logic for leap years
docs(api): update OpenAPI specification with AI categorization endpoints
chore(deps): upgrade Spring Boot to 3.3.4
```

---

## 3. Strict Git Governance Rules

1. **No Fake or Empty Commits:** Commits must represent genuine, verifiable logical units of work. Artificial commit inflation is strictly prohibited.
2. **Never Force-Push to Protected Branches:** `git push --force` or `--force-with-lease` is prohibited on `main`.
3. **Never Rewrite Published History:** Rebase operations must only be executed on local, unpushed feature branches.
4. **Zero Credential Commits:** Never commit `.env` files, passwords, API tokens, private keys, or keystores. The repository relies on `.gitignore` to enforce exclusions.
5. **Verify Before Committing:** Run builds and tests locally prior to creating commits or opening pull requests.

---

## 4. Pull Request (PR) Quality Protocol

When merging feature branches into `main`:
1. **Title:** Follow Conventional Commits format (e.g. `feat(auth): complete Spring Boot 3 JWT authentication and Flyway setup`).
2. **PR Description:** Must document:
   - Summary of technical changes implemented.
   - Modules affected.
   - Verification and testing steps executed.
   - Screenshot / terminal output confirming test pass.
3. **CI Pipeline Pass:** All automated unit and integration tests must pass cleanly.
4. **Merge Method:** "Squash and merge" or "Rebase and merge" to maintain a linear, clean commit history on `main`.
