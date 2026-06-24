# AGENTS.md - OmniAPI Development Guide

> **Scope note:** this file describes what is **already implemented** and verified against the current codebase. For planned/aspirational work (Kubernetes, Kafka, GraphQL, gRPC, WebSockets, etc.), see [`docs/implementation-roadmap.md`](implementation-roadmap.md). Do not add 🔲/planned items here — they belong in the roadmap.

## Project Overview

**OmniAPI** is a **monorepo** with a **layered-monolith** Spring Boot 3.5.8 backend serving Books and Authors data through REST and SOAP, and a React 18 SPA in `frontend/`. The backend is organized by technical layer (`api → service → repository`) rather than by domain module. Java 23 is required.

### Core Stack
- **Framework**: Spring Boot 3.5.8 + Spring Security + Spring Data JPA
- **Databases**: H2 (in-memory development), PostgreSQL (production), SQLite (file-based fallback)
- **Code Generation**: MapStruct for DTO↔Entity mapping, JAXB2 for SOAP models from XSD
- **Testing**: JUnit 5, Mockito, Spring Boot Test with 80% JaCoCo coverage minimum
- **Build**: Maven 3.x (./mvnw wrapper)

---

## Architecture Layers

### Request Flow: Controller → Service → Repository
1. **API Layer** (`api/rest/`, `api/soap/`)
   - REST: `AuthorRESTController`, `BooksRESTController` at `/api/rest` (e.g., GET `/api/rest/authors`)
   - SOAP: `AuthorSOAPController`, `BookSOAPController` at `/api/ws` with namespace `http://spring.io/guides/gs-producing-web-service`

2. **Service Layer** (`service/`)
   - `AuthorService.java`, `BookService.java` - Business logic + transactional boundaries
   - Dependency inject repositories; handle `EntityNotFoundException` at service level
   - Example: `BookService.saveBookAuthor(BookAuthorDTO)` returns void but service handles persistence

3. **Model Layer** (`model/`)
   - **Entities** (`entities/`): JPA `@Entity` classes (e.g., `AuthorEntity`, `BookEntity`) with Lombok `@Data`
   - **DTOs** (`dto/`): Data transfer objects (`AuthorDTO`, `BookDTO`, `BookAuthorDTO`) - use Lombok builders
   - **Mappers** (`mappers/`): MapStruct `@Mapper` interfaces auto-generating entity↔DTO conversions
   - **Builders** (`builders/`): Manual builder classes for complex DTO construction

4. **Repository Layer** (`repository/`)
   - Spring Data JPA extending `JpaRepository<Entity, Long>`
   - Example: `AuthorRepository.findById(Long)` returns `Optional<AuthorEntity>`

### Database Configuration Profiles (`src/main/resources/application-*.properties`)
- **h2**: In-memory, auto-creates schema, pre-populates from `db_scripts/h2data.sql`, enables `/h2-console`
- **postgres**: External PostgreSQL (via Docker), updates existing schema via `PostgresDatabaseLoader` CommandLineRunner
- **sqlite**: File-based persistence to `omniapidb.db`, includes sample data
- **Default**: No profile defaults—must explicitly activate one via Maven `-D` flag or IDE run config

#### PostgreSQL Data Loading (Docker)
The `docker-compose.yml` includes:
1. Health check: `pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"` (checks container readiness)
2. Volume mount: `./src/main/resources/db_scripts/postgres:/docker-entrypoint-initdb.d:ro` (for PostgreSQL init scripts)
3. Java loader: `PostgresDatabaseLoader` executes as `CommandLineRunner` after Hibernate creates tables (Order=3)

**Execution sequence**: Container start → Health check passes → Spring boot starts → Hibernate DDL → `PostgresDatabaseLoader` inserts seed data via `BookService.saveBookAuthor()`

#### Switching Profiles
```bash
# H2 (in-memory)
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"

# PostgreSQL (requires: docker-compose up -d)
docker-compose up -d
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=postgres"

# SQLite
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=sqlite"
```

---

## Key Conventions & Patterns

### Entity-DTO Mapping with MapStruct
- Every `*Entity` has a matching `*DTO` with `@Mapper` interface in `mappers/`
- Mappers use `@Mapping` annotations for non-trivial field mappings (e.g., `@Mapping(source="authorEntity.id", target="authorId")`)
- Services call mapper methods: `BookMapper.bookEntityToBookDTO(entity)` returns DTO

**Example**:
```java
@Entity
public class AuthorEntity {
    @Id Long id;
    @Column String name;
    // Lombok auto-generates getters/setters
}

public class AuthorDTO {
    Long authorId;     // != entity's "id"
    String authorName; // != entity's "name"
}

@Mapper(componentModel = "spring")
public interface AuthorMapper {
    @Mapping(source="id", target="authorId")
    @Mapping(source="name", target="authorName")
    AuthorDTO entityToDTO(AuthorEntity entity);
}
```

### SOAP Namespace & Code Generation
- **WSDL Namespace**: All SOAP endpoints bind to `http://spring.io/guides/gs-producing-web-service`
- **XSD Files** in `src/main/resources/xsd/` auto-generate Java classes in `target/generated-sources/xjc/` with package `bookshelf.generated`
- Controllers use `@Endpoint`, `@PayloadRoot(namespace=..., localPart="...")`, `@ResponsePayload` annotations
- SOAP requests must use correct XML namespace in request payloads

### Exception Handling
- Custom exceptions (e.g., `AuthorServiceException`, `BookValidationException`) in `exceptions/`
- Service layer throws `EntityNotFoundException` for missing records (caught by controllers)
- SOAP fault mapping via `@SoapFault(faultCode = FaultCode.SERVER)`

### Transactional Boundaries
- Mark service methods `@Transactional` when modifying data (e.g., delete operations)
- Rollback on `RuntimeException` (not checked exceptions)

---

## Testing Patterns

### Unit Tests (`src/test/java/**/*Test.java`)
- Use `@ExtendWith(MockitoExtension.class)` for mocking
- REST controller tests: `@WebMvcTest(controllers={...})` with `@MockBean` for services
- Service tests: `@InjectMocks` service + `@Mock` repositories
- Setup mock data in `@BeforeEach`, reset with `reset(mockObj)` if needed
- Assert via `assertEquals()`, `assertNotNull()`, verify calls with `verify(service).method()`

**Pattern**:
```java
@ExtendWith(MockitoExtension.class)
public class AuthorServiceTest {
    @InjectMocks private AuthorService authorService;
    @Mock private AuthorRepository authorRepository;
    
    @Test
    public void testGetAuthorById() {
        AuthorEntity entity = AuthorEntity.builder().id(1L).name("Test").build();
        when(authorRepository.findById(1L)).thenReturn(Optional.of(entity));
        AuthorDTO result = authorService.getAuthorById(1L);
        assertEquals("Test", result.getAuthorName());
    }
}
```

### Integration Tests (`src/test/java/**/*IT.java`)
- Use `@SpringBootTest(webEnvironment = RANDOM_PORT)` with `@ActiveProfiles("h2")`
- SOAP tests: Setup `Jaxb2Marshaller` in `@BeforeEach` with context path `"bookshelf.generated"`
- Create `WebServiceTemplate` and set marshaller/unmarshaller + URI `"http://localhost:" + port + "/api/ws"`
- Call endpoints that return XSD-generated objects

### Test Coverage Requirements
- **JaCoCo minimum**: 80% code coverage (includes line coverage checks)
- **Exclusions** (pre-configured in pom.xml): Generated SOAP classes, DTOs, Entities, `*Application` class, `HelloWorldController`

### Database Tests Setup
- Use `DatabaseConnectionTest.java` to validate connectivity with current profile
- Connection details pulled from active `application-*.properties`

---

## Frontend

A React 18 single-page application lives in `frontend/` at the project root.

### Stack
- **Framework**: React 18 + React Router 6
- **Build tool**: Vite 5
- **Styling**: Tailwind CSS 3
- **HTTP client**: Axios 1.x
- **Dev port**: `http://localhost:5173` (proxies `/api` → `http://localhost:9090`)

### Running the Frontend
```bash
cd frontend
npm install       # first time only
npm run dev       # starts Vite dev server at http://localhost:5173
npm run build     # production build to frontend/dist/
```

### Directory Structure
```
frontend/
├── package.json
├── vite.config.js          # /api proxy → localhost:9090
├── index.html
├── tailwind.config.js
├── postcss.config.js
└── src/
    ├── index.css           # Tailwind directives
    ├── main.jsx            # React root, wraps <AuthProvider>
    ├── App.jsx             # Router: /login → LoginPage, /oauth/callback → OAuthCallbackPage, /admin → AdminPage (protected)
    ├── auth/
    │   └── pkce.js         # Hand-rolled PKCE helper (code_verifier/code_challenge/state via Web Crypto)
    ├── context/
    │   └── AuthContext.jsx # Stores JWT access token in localStorage (key: omniapi_access_token)
    ├── api/
    │   ├── client.js       # Axios instance, injects Authorization: Bearer header on every request
    │   ├── authorsApi.js   # getAuthors, getAuthor, createAuthor, updateAuthor, deleteAuthor
    │   └── booksApi.js     # getBooks, addBook, addBookAuthor, updateBook, deleteBook
    ├── pages/
    │   ├── LoginPage.jsx        # Redirects to the backend's /oauth2/authorize with PKCE params
    │   ├── OAuthCallbackPage.jsx # Exchanges the authorization code for a JWT at /oauth2/token
    │   └── AdminPage.jsx        # Authors | Books tabs with Sign Out
    └── components/
        ├── authors/
        │   ├── AuthorList.jsx  # Table with inline Edit/Delete; opens AuthorForm panel
        │   └── AuthorForm.jsx  # Shared create/update form (fields: authorName, dateOfBirth, countryOfOrigin)
        └── books/
            ├── BookList.jsx    # Table with inline Edit/Delete; opens BookForm panel
            └── BookForm.jsx    # Shared create/update form; author selected from dropdown
```

### Authentication Flow
1. `LoginPage.jsx` renders a username/password form inside the React/Tailwind SPA.
2. On submit, it POSTs `{username, password}` JSON to `POST http://localhost:9090/api/auth/login` (`AuthController.java`) with `credentials: 'include'`. The backend validates against the `app_user` table via `OmniApiUserDetailsService`, establishes a Spring Security session (sets `JSESSIONID` cookie), and returns `200` or `401 {"error":"Invalid credentials"}`. Invalid credentials show an inline error in the React form — the user never leaves the SPA.
3. On `200`, `LoginPage.jsx` generates PKCE params (`src/auth/pkce.js`), stores `code_verifier`/`state` in `sessionStorage`, and does `window.location.assign(http://localhost:9090/oauth2/authorize?...)`. Because the `JSESSIONID` cookie is sent with this navigation, Spring sees the already-authenticated session and issues an authorization code immediately — **Spring's `/login` page is never shown**.
4. The browser is redirected to `http://localhost:5173/oauth/callback?code=...&state=...`.
5. `OAuthCallbackPage.jsx` validates `state`, exchanges the code at `POST http://localhost:9090/oauth2/token` (grant_type=authorization_code + code_verifier), and stores the JWT via `AuthContext.setTokens(...)`, navigating to `/admin`.
6. `client.js` reads `omniapi_access_token` from localStorage and injects `Authorization: Bearer <token>` on every `/api/rest/**` request; a `401` response clears storage and redirects to `/login`.
7. Logout clears localStorage and redirects to `/login`. Re-authentication after token expiry (15 min) is always a fresh round trip — no silent refresh (no refresh tokens are issued for public PKCE clients by Spring Authorization Server).
- **Credentials**: loaded from the `app_user` table. H2/SQLite profiles seed `admin`/`admin` via `UserDataLoader` on first startup. Postgres uses `OMNIAPI_ADMIN_USERNAME`/`OMNIAPI_ADMIN_PASSWORD` from `.env` (defaults to `admin`/`admin` if unset).

### Adding a New Frontend Feature
1. Add the API function in `src/api/authorsApi.js` or `booksApi.js` (mirrors a backend endpoint).
2. Create or update the component in `src/components/`.
3. Import and render it in the relevant page (`AdminPage.jsx` or a new page).
4. Add a `<Route>` in `App.jsx` if it's a new page.

## Build & Development Workflow

### Maven Commands
```bash
# Full build with tests
./mvnw clean package

# Build without tests
./mvnw clean package -DskipTests

# Run specific profile
./mvnw clean package -Ph2  # Sets spring.profiles.active=h2

# Run unit tests only (Surefire)
./mvnw test

# Run integration tests only (Failsafe, *IT.java)
./mvnw verify

# Check test coverage report
./mvnw verify  # Generates to target/site/jacoco/index.html
```

### IDE Run Configurations
**IntelliJ IDEA** includes pre-configured "OmniAPI (H2)", "OmniAPI (PostgreSQL)", "OmniAPI (SQLite)" run configs—select from dropdown and Run.

### Docker for PostgreSQL
```bash
docker-compose up -d      # Starts PostgreSQL on port 5432
docker-compose down       # Stops container
# Credentials: read from .env (gitignored, not documented here)
```

---

## Security & Authorization

- **Framework**: Spring Security with three `SecurityFilterChain` beans outside the test profile (see `docs/Security.md` for OWASP mapping)
- **`AuthorizationServerConfig.java`** (`security/oauth/`, `@Profile("!test")`, `@Order(1)`) — OmniAPI's self-hosted OAuth2 Authorization Server (`spring-security-oauth2-authorization-server`). Registers one public PKCE client (`omniapi-spa`), an in-memory-generated RSA `JWKSource`/`JwtDecoder`, and `AuthorizationServerSettings`. Matches only `/oauth2/*` and OIDC discovery endpoints.
- **`OmniApiUserDetailsService.java`** (`security/`) — implements `UserDetailsService`, queries the `app_user` table via `UserRepository`. Replaces the removed `InMemoryUserDetailsManager`.
- **`AuthController.java`** (`api/auth/`) — `POST /api/auth/login` JSON endpoint used by the React login form. Validates credentials via `AuthenticationManager`, establishes a Spring Security session, returns 200/401. Not under `/api/rest/**` so it requires no JWT.
- **`SecurityConfig.java`** (`@Profile("!test")`):
  - `@Order(2)` `resourceServerFilterChain` — matches `/api/rest/**` and `/api/ws/**`, `SessionCreationPolicy.STATELESS` (no session created or read), bearer JWT required (`oauth2ResourceServer().jwt()`). JSESSIONID cannot authenticate here.
  - `@Order(3)` `defaultFilterChain` — catch-all `/**`, `formLogin()` serves `/login` for the OAuth2 resource-owner step, `anyRequest().permitAll()` for SOAP, actuator, `/api/auth/login`, etc.
- **Test Profile**: `TestSecurityConfig` with `@Profile("test")` permits all requests for testing
- **CSRF**: Disabled on all three chains (REST API + SPA clients don't use CSRF cookies)

### Key Filters (In Order)
1. `SecurityContextHolderFilter` → Load authentication
2. `AuthorizationFilter` → Check permissions (A01 Broken Access Control)
3. `BearerTokenAuthenticationFilter` → Validate JWT on `/api/rest/**` (A07)
4. `UsernamePasswordAuthenticationFilter` → Process `/login` form submissions for the OAuth2 resource-owner step (A07)
5. `HeaderWriterFilter` → Add security headers (A03, A05)
6. `ExceptionTranslationFilter` → Handle auth errors (A05)

Note: `CsrfFilter` is **not** in any non-test chain — all three call `.csrf(AbstractHttpConfigurer::disable)`. `UsernamePasswordAuthenticationFilter` is present only on chain 3, not on the Resource Server chain — a JSESSIONID cannot authenticate against `/api/rest/**` or `/api/ws/**`.

### HTTPS / TLS (opt-in)

Off by default — local dev, tests, and CI all keep running on plain HTTP unless explicitly enabled.

- **Enable**: set `SSL_ENABLED=true` and `SSL_KEYSTORE_PASSWORD=<password>` (both in `.env`, gitignored) before running the app.
- **Keystore**: self-signed PKCS12 cert at `backend/keystore/omniapi.p12` (gitignored — not committed). Regenerate it with:
  ```bash
  keytool -genkeypair -alias omniapi -keyalg RSA -keysize 2048 -storetype PKCS12 \
    -keystore backend/keystore/omniapi.p12 -validity 3650 \
    -dname "CN=localhost, OU=OmniAPI, O=OmniAPI, L=Dev, ST=Dev, C=US" \
    -storepass "$SSL_KEYSTORE_PASSWORD" -keypass "$SSL_KEYSTORE_PASSWORD"
  ```
- **Behavior when enabled**: `server.port` (9090) serves HTTPS; `HttpToHttpsRedirectConfig` (`security/`) adds a second Tomcat connector on `server.http.port` (8080, override via `HTTP_PORT`) with a `SecurityConstraint` requiring confidential transport, so Tomcat redirects HTTP → HTTPS at the container level — the modern replacement for Spring Security's deprecated `requiresChannel()`. `SecurityConfig` explicitly sets HSTS (`includeSubDomains`, 1-year max-age).
- **Browser/client note**: the cert is self-signed, so browsers/`curl` will warn — use `curl -k` or trust the cert locally.

---

## Common Tasks

### Add a New REST Endpoint
1. Create method in service (e.g., `AuthorService.searchByName(String)`)
2. Add `@GetMapping` or `@PostMapping` in controller (e.g., `AuthorRESTController`)
3. Map entities to DTOs in controller before response
4. Write unit test for controller + service layer
5. Ensure 80% coverage; run `./mvnw verify`

### Add a New SOAP Operation
1. Define XSD element/complexType in `src/main/resources/xsd/` (e.g., add to `bookshelf.xsd`)
2. Run `./mvnw clean compile` to auto-generate Java classes in `target/generated-sources/xjc/bookshelf/generated/`
3. Create `@PayloadRoot(@RequestPayload)` + `@ResponsePayload` method in SOAP controller
4. Use correct namespace: `http://spring.io/guides/gs-producing-web-service`
5. Write integration test in `*IT.java` with `Jaxb2Marshaller`

### Switch Databases for Testing
- Edit IDE run config to use different `-Dspring-boot.run.arguments="--spring.profiles.active=postgres"`
- Or set Maven profile: `./mvnw test -Ppostgres`
- PostgreSQL requires `docker-compose up -d` first

### Fix Test Coverage Issues
1. Run `./mvnw verify` to generate JaCoCo report
2. Open `target/site/jacoco/index.html` in browser
3. Focus on untested lines in service/controller classes (DTOs/Entities excluded)
4. Add test cases in `*Test.java` (unit) or `*IT.java` (integration)

---

## File Organization Summary

```
frontend/                                   # React SPA (Vite + Tailwind)
├── src/
│   ├── context/AuthContext.jsx             # Auth state + localStorage
│   ├── api/                                # Axios wrappers for REST endpoints
│   ├── pages/LoginPage.jsx                 # Login form
│   ├── pages/AdminPage.jsx                 # Authors | Books admin tabs
│   └── components/authors/ books/         # List + Form components per entity

src/main/java/com/messalas/omniapi/
├── SpringBootDemoAApplication.java         # Entry point
├── api/
│   ├── auth/                               # Auth endpoints (login, not under /api/rest/**)
│   │   ├── AuthController.java             # POST /api/auth/login → 200/401 JSON
│   │   └── LoginRequest.java               # Record {username, password}
│   ├── rest/                               # REST endpoints (@RestController)
│   └── soap/                               # SOAP endpoints (@Endpoint)
├── service/                                # Business logic (@Service)
├── repository/                             # Data access (JpaRepository)
├── model/
│   ├── entities/                           # JPA @Entity classes
│   ├── dto/                                # DTOs for API contracts
│   ├── mappers/                            # MapStruct @Mapper interfaces
│   └── builders/                           # Manual builders
├── security/                               # Spring Security config
│   ├── OmniApiUserDetailsService.java      # Implements UserDetailsService → queries app_user table
│   └── oauth/AuthorizationServerConfig.java # Self-hosted OAuth2 Authorization Server
├── exceptions/                             # Custom exceptions + SOAP faults
└── db/                                     # Database loaders (CommandLineRunner)
    └── UserDataLoader.java                 # Seeds default admin user if app_user is empty (all profiles)

src/main/resources/
├── application*.properties                 # Profile-specific config
├── db_scripts/                             # SQL initialization (h2data.sql, sqlitedata.sql)
└── xsd/                                    # SOAP schema files (auto-gen Java code)

src/test/java/com/messalas/omniapi/
├── DatabaseConnectionTest.java             # Profile connectivity checks
├── unit/                                   # Unit tests (*Test.java, @WebMvcTest/@MockBean)
│   └── mappers/                            # MapStruct mapper unit tests
└── integration/                            # Integration tests (*IT.java, @SpringBootTest, real DB)
```

---

## Debugging Tips

1. **Enable SQL logging**: Set `spring.jpa.show-sql=true` in profile properties (already enabled for debugging profiles)
2. **Check active profile**: Most issues stem from wrong profile—use `./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"` explicitly
3. **SOAP namespace mismatch**: Verify XSD file namespace URL matches `@PayloadRoot(namespace=...)` in controller
4. **MapStruct code not generated**: Run `./mvnw clean compile` to trigger annotation processing; check `target/generated-sources/xjc/`
5. **H2 console**: When using h2 profile, browse to `http://localhost:9090/h2-console` (credentials: user=sa, password=password)
6. **Build failures**: Always run `./mvnw clean` before `package` if stale generated classes cause issues

---

## Database Troubleshooting & Development

### Access PostgreSQL Container CLI

To troubleshoot or run SQL commands directly against the running Postgres container:

```bash
# Connect to psql inside the running container (reads POSTGRES_USER/POSTGRES_DB from .env)
docker exec -it omniapi-postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"

# Run a query directly
docker container exec -it omniapi-postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c  "SELECT * FROM user";
```

Once connected, you can run SQL queries:
```sql
-- Check authors table
SELECT * FROM author;

-- Check books table
SELECT * FROM book;

-- Count rows
SELECT COUNT(*) FROM author;
SELECT COUNT(*) FROM book;

-- List all tables
\dt

-- Exit psql
\q
```

### Quick Commands (One-Liners)

```bash
# Query without entering psql shell
docker exec omniapi-postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT COUNT(*) FROM author;"

# Dump database schema
docker exec omniapi-postgres pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" --schema-only

# Check if Postgres is ready
docker exec omniapi-postgres pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"
```

### Environment Variables
Defined in `.env` (gitignored — actual values are never committed; read them locally if needed):
- **POSTGRES_USER**
- **POSTGRES_DB**
- **POSTGRES_PASSWORD**
- **Host**: `localhost` (from host machine), `test-postgres` (from Docker network in CI)
- **Port**: `5432`

---

## Key Dependencies & Versions
- Spring Boot: **3.5.8**
- Java: **23** (target/source in pom.xml)
- PostgreSQL JDBC: **42.7.4**
- SQLite JDBC: **3.46.0.0**
- MapStruct: **1.6.3**
- Lombok: **1.18.34**
- JaCoCo: **0.8.14** (80% coverage minimum)
- Spring WS: **4.0.11** (SOAP framework)

---

## External Resources
- **Security Deep Dive**: `docs/Security.md` (OWASP Top 10 mapping & filter chain)
- **Getting Started**: `README.md` (quick start commands for each database profile)
- **Docker Setup**: `docker-compose.yml` (PostgreSQL only—H2/SQLite don't require containers)

