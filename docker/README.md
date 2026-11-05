# Expense Tracker - Docker & Infrastructure

## Overview
This directory contains containerization manifests, multi-stage Dockerfiles, and orchestration compose files for the **Expense Tracker** application stack.

## Architecture Orchestration
- **PostgreSQL 16:** Database container with health checks and volume mounts.
- **Backend Service:** Multi-stage Java 21 / Eclipse Temurin runtime container.
- **AI Service:** Python 3.11-slim FastAPI container with pre-warmed ML inference workers.
- **Frontend Service:** Multi-stage Node.js build container served via Nginx reverse proxy.

## Planned Execution (Phase 10)
```bash
# Start full local application stack
docker compose up -d --build

# View stack status
docker compose ps

# View container logs
docker compose logs -f
```
