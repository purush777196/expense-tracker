# Expense Tracker - Backend Module

## Overview
This directory contains the core REST API backend for **Expense Tracker**, engineered using Java 21, Spring Boot 3, and Spring Security.

## Technology Stack
- **Language:** Java 21 LTS
- **Framework:** Spring Boot 3.3.x
- **Security:** Spring Security 6, JJWT (JSON Web Token)
- **Persistence:** Spring Data JPA, Hibernate ORM
- **Database:** PostgreSQL 16
- **Database Migrations:** Flyway
- **Validation:** Jakarta Bean Validation (Hibernate Validator)
- **Build Tool:** Apache Maven

## Architecture & Design
- Detailed system architecture: [docs/architecture/system-architecture.md](../docs/architecture/system-architecture.md)
- REST API specification: [docs/api/api-specification.md](../docs/api/api-specification.md)
- Database design: [docs/database/database-design.md](../docs/database/database-design.md)

## Planned Setup (Phase 1)
```bash
# Build the project
./mvnw clean package

# Run the Spring Boot application
./mvnw spring-boot:run
```
