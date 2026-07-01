# OmniAPI

> **Template Repository** — OmniAPI is a production-style Spring Boot + React starter template designed to be used as a GitHub template. It provides authentication, persistence, testing, and infrastructure out of the box so you can replace the Books/Authors domain with your own application.

OmniAPI is a **monorepo** containing a **Spring Boot 3** backend and a **React 18** frontend. Its core idea is simple:

> **One service layer exposed through multiple API paradigms.**

Today the project supports **REST** and **SOAP** over the same business logic. Future releases will add **GraphQL**, **gRPC**, and **WebSockets** without duplicating domain logic.

---

# Why OmniAPI?

Most starter templates demonstrate CRUD.

OmniAPI demonstrates **architecture**.

It is designed to showcase production-oriented engineering practices rather than a business domain.

Highlights include:

* REST and SOAP sharing the same service layer
* OAuth2 Authorization Server with PKCE
* JWT Resource Server
* Multi-database support (H2, PostgreSQL, SQLite)
* React SPA with protected administration UI
* Docker support
* Comprehensive testing
* DevSecOps pipeline
* Security-first architecture
* Built as a reusable project template

---

# Architecture

```
                    React SPA
                       │
                 OAuth2 + PKCE
                       │
                Spring Security
                       │
        ┌──────────────┴──────────────┐
        │                             │
      REST                         SOAP
        │                             │
        └──────────────┬──────────────┘
                       │
                 Service Layer
                       │
                 MapStruct DTOs
                       │
                Spring Data JPA
                       │
      H2 • PostgreSQL • SQLite
```

The application follows a layered architecture:

```
API
    ↓
Service
    ↓
Repository
    ↓
Database
```

Business logic exists only in the service layer. Every API protocol communicates with the same services, avoiding duplicated implementation.

For a detailed explanation of the architecture, project layout, conventions, and extension points, see **docs/ARCHITECTURE.md**.

---

# Features

## Backend

* Spring Boot 3
* Spring Security
* OAuth2 Authorization Server
* JWT Resource Server
* Spring Data JPA
* REST API
* SOAP API
* MapStruct
* Bean Validation
* Optimistic Locking
* Idempotency Keys
* Global Exception Handling
* Multi-profile configuration

## Frontend

* React 18
* React Router
* Tailwind CSS
* Axios
* OAuth2 Authorization Code + PKCE
* Protected Admin UI

## Infrastructure

* Docker
* Docker Compose
* GitHub Actions
* GHCR image publishing
* Trivy vulnerability scanning
* Gitleaks secret scanning
* JaCoCo coverage
* PostgreSQL integration testing

---

# Quick Start

## Prerequisites

| Tool    | Version                         |
| ------- | ------------------------------- |
| Java    | 23+                             |
| Maven   | 3.x                             |
| Node.js | 18+                             |
| Docker  | Required for PostgreSQL profile |

---

## Backend

### H2

```bash
./mvnw spring-boot:run \
-Dspring-boot.run.arguments="--spring.profiles.active=h2"
```

### PostgreSQL

```bash
cp .env.example .env

docker-compose up -d

./mvnw spring-boot:run \
-Dspring-boot.run.arguments="--spring.profiles.active=postgres"
```

### SQLite

```bash
./mvnw spring-boot:run \
-Dspring-boot.run.arguments="--spring.profiles.active=sqlite"
```

---

## Frontend

```bash
cd frontend

npm install

npm run dev
```

The frontend runs on:

```
http://localhost:5173
```

The backend runs on:

```
http://localhost:9090
```

(Optional HTTPS is available. See **docs/SECURITY.md**.)

---

# Default Credentials

| Profile    | Username              | Password |
| ---------- | --------------------- | -------- |
| H2         | admin                 | admin    |
| SQLite     | admin                 | admin    |
| PostgreSQL | Configured via `.env` |          |

---

# Project Structure

```
frontend/
docs/
src/
docker-compose.yml
pom.xml
```

Important directories:

```
src/main/java/
    api/
    service/
    repository/
    model/
    security/

frontend/src/
    api/
    components/
    context/
    pages/
```

A complete architectural breakdown is available in **docs/ARCHITECTURE.md**.

---

# Technology Stack

## Backend

* Spring Boot
* Spring Security
* Spring Authorization Server
* Spring Data JPA
* Spring Web Services
* MapStruct
* Lombok
* H2
* PostgreSQL
* SQLite
* JUnit 5
* Mockito
* JaCoCo

## Frontend

* React
* React Router
* Tailwind CSS
* Axios
* Vite

---

# Documentation

| Document               | Purpose                                                                                             |
| ---------------------- | --------------------------------------------------------------------------------------------------- |
| `docs/ARCHITECTURE.md` | Project architecture, code organization, development conventions, testing, and extension points     |
| `docs/API.md`          | REST and SOAP contracts, authentication, request/response examples, idempotency, optimistic locking |
| `docs/SECURITY.md`     | Authentication, authorization, OAuth2, OWASP mapping, HTTPS, DevSecOps, security controls           |
| `docs/COMPLIANCE.md`   | Implemented and planned engineering controls for GDPR, NIS2, DORA, and related standards            |
| `docs/ROADMAP.md`      | Planned features and future direction                                                               |

---

# Roadmap

The long-term goal is to expose the same business layer through every major API paradigm.

Current:

* REST
* SOAP

Planned:

* GraphQL
* WebSockets
* gRPC
* Redis caching
* Elasticsearch
* Kubernetes deployment
* Cloud infrastructure
* SonarQube quality gates

See **docs/ROADMAP.md** for details.

---

# Design Goals

OmniAPI is intended to demonstrate:

* Layered Architecture
* Separation of Concerns
* SOLID Principles
* ACID Transactions
* Secure Authentication
* API Contract Design
* Multi-database Support
* Production-oriented Testing
* DevSecOps Practices
* Extensible Software Design

The Books/Authors domain is intentionally simple so the engineering decisions remain the focus.

---

# Contributing

Contributions, bug reports, and suggestions are welcome.

Please open an issue before submitting significant architectural changes.

---

# License

MIT License.
