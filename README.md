# OmniAPI

> **Template repository** — OmniAPI is designed to be used as a GitHub template. Click **Use this template** to bootstrap a new project with the full stack (Spring Boot + React + OAuth2 + multi-database support) already wired up. Swap out the Books/Authors domain for your own, and extend from there.

A **monorepo** containing a **layered-monolith** Spring Boot 3.5.8 backend and a React 18 SPA. The backend is organized by technical layer (`api → service → repository`) rather than by domain module. The project's thesis: one service layer, every major API paradigm — REST and SOAP today, with GraphQL, gRPC, and WebSockets on the [roadmap](docs/implementation-roadmap.md).

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 23+ |
| Maven | 3.x (or use `./mvnw`) |
| Node.js | 18+ |
| Docker | Required for PostgreSQL profile only |

---

## Quick Start

### 1. Backend

**H2 (in-memory — no setup required):**
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"
```

**PostgreSQL (requires Docker):**
```bash
cp .env.example .env          # fill in credentials (or keep the defaults)
docker-compose up -d
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=postgres"
```

**SQLite (file-based — no setup required):**
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=sqlite"
```

Backend listens on **`https://localhost:9090`** (HTTP by default; see [HTTPS](#https--tls) below).

IntelliJ IDEA users: pre-configured run configs `OmniAPI (H2)`, `OmniAPI (PostgreSQL)`, and `OmniAPI (SQLite)` are available from the dropdown.

### 2. Frontend

```bash
cd frontend
npm install       # first time only
npm run dev       # starts Vite dev server at http://localhost:5173
```

The Vite dev server proxies `/api` → `http://localhost:9090`, so the backend must be running.

### 3. Default Credentials

| Profile | Username | Password | Source |
|---------|----------|----------|--------|
| H2 | `admin` | `admin` | `UserDataLoader` seeds on first run |
| SQLite | `admin` | `admin` | `UserDataLoader` seeds on first run |
| PostgreSQL | `OMNIAPI_ADMIN_USERNAME` env | `OMNIAPI_ADMIN_PASSWORD` env | `.env` (defaults to `admin`/`admin`) |

---

## Authentication Flow

OmniAPI hosts its **own OAuth2 Authorization Server** (Spring Authorization Server). The React SPA uses the OAuth 2.0 Authorization Code + PKCE flow — no external IdP required.

1. User submits the login form → `POST /api/auth/login` validates against the `app_user` table and sets a session cookie.
2. The SPA navigates to `/oauth2/authorize` with PKCE params; the session cookie satisfies the auth check, so the Spring login page is never shown.
3. The browser is redirected to `/oauth/callback`, where the SPA exchanges the code for a JWT at `/oauth2/token`.
4. The JWT (15-minute TTL) is stored in `localStorage` and injected as `Authorization: Bearer …` on every `/api/rest/**` request.

---

## API Endpoints

### REST — `/api/rest`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/rest/authors` | List all authors |
| GET | `/api/rest/author/{id}` | Get author by ID |
| POST | `/api/rest/createAuthor` | Create author |
| PUT | `/api/rest/authors/{id}` | Update author (requires `version`) |
| DELETE | `/api/rest/authors/{id}` | Delete author |
| GET | `/api/rest/books` | List all books |
| GET | `/api/rest/book/{name}` | Get book by name |
| POST | `/api/rest/addBook` | Create book |
| PUT | `/api/rest/books/{id}` | Update book (requires `version`) |
| DELETE | `/api/rest/books/{id}` | Delete book |

All `/api/rest/**` endpoints require a valid JWT bearer token.

#### Optimistic Locking on PUT

Both PUT endpoints use optimistic locking to prevent lost updates under concurrent writes.

Every GET and POST response includes a `version` field. Pass this value back unchanged in the PUT request body:

```json
PUT /api/rest/books/1
{
  "version": 0,
  "bookName": "Clean Code",
  "publicationYear": "2008",
  "authorDTO": { "authorName": "Robert Martin" }
}
```

| Response | Meaning |
|----------|---------|
| `200 OK` | Update succeeded; body contains the saved entity with the new incremented `version`. |
| `400 Bad Request` | `version` field was missing or null. |
| `409 Conflict` | Another request updated this record after your last fetch. Re-fetch to get the current `version` and retry. |

The same contract applies to the SOAP `UpdateBookRequest` / `UpdateAuthorRequest` operations, where a version mismatch returns a `CLIENT` fault.

### SOAP — `/api/ws`

WSDL available at `http://localhost:9090/api/ws/bookshelf.wsdl`. Namespace: `http://spring.io/guides/gs-producing-web-service`.

Operations: `CreateBookAuthorRequest`, `CreateBookRequest`, `UpdateBookRequest`, `DeleteBookRequest`, `GetBookRequest`, `GetBooksRequest`, `CreateAuthorRequest`, `UpdateAuthorRequest`, `DeleteAuthorRequest`, `GetAuthorRequest`, `GetAuthorsRequest`.

---

## Database Profiles

| Profile | Database | Persistence | Setup | Sample Data |
|---------|----------|-------------|-------|-------------|
| `h2` | H2 in-memory | Lost on restart | None | Seeded automatically |
| `postgres` | PostgreSQL | Persists | Docker | Seeded by `PostgresDatabaseLoader` |
| `sqlite` | SQLite file | Persists | None | Seeded automatically |

H2 console (h2 profile only): `http://localhost:9090/h2-console` — user `sa`, password `password`.

---

## HTTPS / TLS

Off by default. To enable, add to `.env`:

```
SSL_ENABLED=true
SSL_KEYSTORE_PASSWORD=<your-password>
```

Then generate the self-signed keystore (one time):

```bash
keytool -genkeypair -alias omniapi -keyalg RSA -keysize 2048 -storetype PKCS12 \
  -keystore backend/keystore/omniapi.p12 -validity 3650 \
  -dname "CN=localhost, OU=OmniAPI, O=OmniAPI, L=Dev, ST=Dev, C=US" \
  -storepass "$SSL_KEYSTORE_PASSWORD" -keypass "$SSL_KEYSTORE_PASSWORD"
```

When enabled: HTTPS on port 9090, HTTP on port 8080 redirects to HTTPS, HSTS header included.

---

## Testing

```bash
# Unit tests only
./mvnw test

# Integration tests + coverage report
./mvnw verify

# Coverage report → target/site/jacoco/index.html (80% minimum enforced)
```

---

## Stack

**Backend**
- Spring Boot 3.5.8, Spring Security, Spring Data JPA
- Spring Authorization Server (self-hosted OAuth2 AS)
- Spring Web Services 4.0.11 (SOAP)
- MapStruct 1.6.3, Lombok 1.18.34
- H2 / PostgreSQL 42.7.4 / SQLite 3.46.0.0
- JUnit 5, Mockito, JaCoCo 0.8.14

**Frontend**
- React 18, React Router 6
- Vite 5, Tailwind CSS 3
- Axios 1.x

---

## Documentation

- [AGENTS.md](docs/AGENTS.md) — architecture, conventions, testing patterns, debugging tips
- [Security.md](docs/Security.md) — OWASP Top 10 mapping, filter chain details, security implementation status
- [implementation-roadmap.md](docs/implementation-roadmap.md) — planned protocols (GraphQL, gRPC, WebSockets) and features
