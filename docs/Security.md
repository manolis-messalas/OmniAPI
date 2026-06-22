# Application Security Overview

> **Scope note:** like [`implementation-roadmap.md`](implementation-roadmap.md), this file separates what is **verified against the current codebase** (Section 1) from what is **planned** (Section 2). Don't move an item from Section 2 to Section 1 without checking the actual source first.

This application uses Spring Security as the core framework for authentication, authorization, and request protection. The OWASP Top 10 (a regularly-updated industry report on the most critical web application security risks) is used as the reference framework for prioritizing and categorizing security work below.

### OWASP Top 10 Reference
A01 Broken Access Control · A02 Cryptographic Failures · A03 Injection · A04 Insecure Design · A05 Security Misconfiguration · A06 Vulnerable and Outdated Components · A07 Identification and Authentication Failures · A08 Software and Data Integrity Failures · A09 Security Logging and Monitoring Failures · A10 Server-Side Request Forgery (SSRF)

---

## Section 1: Already Implemented

### 1a. Application Security

- **CORS lockdown** — only `http://localhost:5173` is allowed as an origin, explicit method/header allow-list. *(A05)*
- **Default security headers** — `HeaderWriterFilter` is active (not disabled), so Spring's baseline response headers (e.g. `X-Content-Type-Options`, `X-Frame-Options`) are applied. *(A05)*
- **Rate limiting** — `RateLimitFilter` (`security/RateLimitFilter.java`, registered via `FilterRegistrationBean` in `SecurityConfig`, `@Profile("!test")`) runs before Spring Security (servlet filter order -101). Uses Bucket4j 8.10.1 token-bucket algorithm with in-memory per-IP buckets (`ConcurrentHashMap`). Two limits: `POST /api/auth/login` → 5 requests/minute/IP; write operations (POST/PUT/DELETE/PATCH) on `/api/rest/**` → 20 requests/minute/IP. Blocked requests receive HTTP 429 with `Retry-After` and `{"error":"Too many requests"}`. Read (`GET`) requests are not throttled. `X-Forwarded-For` is used for IP resolution when present (first hop), falling back to `RemoteAddr`. Note: the in-memory map grows with unique IPs — acceptable for the current single-instance scope; a distributed cache (Redis + Bucket4j's proxy) would be the upgrade path for multi-instance deployments. *(A04, A07)*
- **No SSRF surface** — neither the REST nor SOAP controllers make outbound HTTP calls; there is no attacker-controllable URL/host/path for the server to call. *(A10)*
- **Test isolation** — `TestSecurityConfig` (`@Profile("test")`) permits all requests, keeping security concerns out of unrelated unit/integration tests.
- **HTTPS / TLS (opt-in)** — `server.ssl.enabled=${SSL_ENABLED:false}` in `application.properties`, backed by a self-signed PKCS12 keystore (`backend/keystore/`, gitignored, password in `.env`). Off by default so local dev/CI keep using plain HTTP unchanged. When enabled: `HttpToHttpsRedirectConfig` adds a second Tomcat connector on `server.http.port` (8080) with a `SecurityConstraint` requiring confidential transport, so Tomcat itself redirects HTTP → HTTPS (the modern replacement for Spring Security's deprecated `requiresChannel()`); `SecurityConfig` explicitly configures HSTS (`includeSubDomains`, 1-year max-age) via `.headers(...)`. Verified: HTTPS responds 200 with `Strict-Transport-Security` header, plain HTTP on 8080 returns a 302 to the HTTPS URL, OAuth2 session cookie's `Secure` flag works correctly over TLS. *(A02)*
- **Centralized exception handling** — `RESTExceptionHandler` (`exceptions/RESTExceptionHandler.java`) is a `@RestControllerAdvice` with handlers for `IllegalArgumentException` (400), `EntityNotFoundException` (404), `MethodArgumentTypeMismatchException` (400), and a generic catch-all (500) that logs the full exception server-side but returns only `"An unexpected error occurred"` to the caller, preventing stack-trace leakage. Uses a custom `ErrorResponse` body (status, message, timestamp) rather than `ProblemDetail`. *(A05)*

#### Authentication

- **DB-backed authentication** — `OmniApiUserDetailsService` (`security/OmniApiUserDetailsService.java`) implements Spring Security's `UserDetailsService`, loading credentials from the `app_user` table via `UserRepository`. The hardcoded `InMemoryUserDetailsManager` is removed. `UserService.saveUser()` BCrypt-hashes passwords before persisting, so credentials stored via the REST API are always hashed. A `UserDataLoader` (`db/UserDataLoader.java`) seeds a default admin user on startup if `app_user` is empty; credentials are configurable via `omniapi.admin.username`/`omniapi.admin.password` (env vars `OMNIAPI_ADMIN_USERNAME`/`OMNIAPI_ADMIN_PASSWORD`, defaulting to `admin`/`admin` for H2/SQLite dev profiles). *(A02, A07)*
- **OAuth 2.0 Authorization Code + PKCE, JWT access tokens** — OmniAPI is its own self-hosted OAuth2 Authorization Server (`spring-security-oauth2-authorization-server`, `security/oauth/AuthorizationServerConfig.java`), not a delegation to an external IdP. A single public client (`omniapi-spa`, `ClientAuthenticationMethod.NONE`, PKCE mandatory, no client secret) is registered in-memory. `/api/rest/**` is a Resource Server (`oauth2ResourceServer().jwt()` in `SecurityConfig`) validating JWTs signed with an RSA keypair generated in-memory at application startup — **documented limitation**: the signing key rotates on every restart, invalidating prior tokens; acceptable given the 15-minute access-token TTL, swap-in point is a persisted key (mirrors the existing `keystore/omniapi.p12` TLS pattern). No refresh tokens: Spring Authorization Server hardcodes a refusal to issue them to public clients (`ClientAuthenticationMethod.NONE`) on the `authorization_code` grant. The React SPA posts credentials to `POST /api/auth/login` (`api/auth/AuthController.java`), which validates via `AuthenticationManager` (backed by `OmniApiUserDetailsService`), establishes a Spring Security session, and returns 200/401 JSON — the browser then navigates to `/oauth2/authorize` with the session cookie already set, so the Spring login page is **never shown to the user**. *(A07)*
- **Resource Server session isolation** — the `/api/rest/**` `SecurityFilterChain` (`@Order(2)`) runs with `SessionCreationPolicy.STATELESS`: Spring Security never creates or reads a session for these paths. A `JSESSIONID` cookie from the OAuth2 login step cannot authenticate against the API — bearer JWT is the only accepted credential on that chain. This eliminates the CSRF-exposed session bypass path that existed when `formLogin` and `oauth2ResourceServer` shared a single chain with CSRF disabled. The OAuth2 login chain (`@Order(3)`) retains default session behaviour; session-fixation protection is active via Spring Security's `migrateSession()` default. *(A07)*

#### Authorization

- **URL-based access rule** — `/api/rest/**` requires authentication; all other requests permitted. Enforced via `AuthorizationFilter`. *(A01)*

**Active filter chains (verified against `AuthorizationServerConfig`/`SecurityConfig`/`TestSecurityConfig`, not the Spring Security default set):** there are now three `SecurityFilterChain` beans outside the test profile — `@Order(1)` in `AuthorizationServerConfig` matches only the Authorization Server's own endpoints (`/oauth2/*`, OIDC discovery); `@Order(2)` in `SecurityConfig` matches `/api/rest/**` exclusively as a `STATELESS` Resource Server (bearer JWT only, session never created or consulted); `@Order(3)` in `SecurityConfig` is the catch-all that serves `/login` via `formLogin()` and permits everything else.
```mermaid
flowchart TD
    Client([Client])

    subgraph Tomcat["Servlet Container · Tomcat"]
        REDIR["HTTP :8080 — SecurityConstraint\n302 redirect → HTTPS :8443\nHttpToHttpsRedirectConfig"]
        RLF["RateLimitFilter · order -101\nPOST /api/auth/login: 5 req/min/IP\nwrite ops /api/rest/**: 20 req/min/IP"]
        DFP["DelegatingFilterProxy · order -100"]
    end

    subgraph SpringSec["Spring Security"]
        subgraph C1["SecurityFilterChain @Order(1) — AuthorizationServerConfig\n/oauth2/** · /.well-known/**"]
            O1["OAuth2AuthorizationEndpointFilter"] --> O2["OAuth2TokenEndpointFilter"] --> O3["OidcProviderConfigurationEndpointFilter"]
        end
        subgraph C2["SecurityFilterChain @Order(2) — SecurityConfig\n/api/rest/** · STATELESS"]
            S1["CorsFilter"] --> S2["HeaderWriterFilter\n(HSTS · X-Frame-Options · X-Content-Type-Options)"] --> S3["BearerTokenAuthenticationFilter\n(JWT on Authorization: Bearer)"] --> S5["AnonymousAuthenticationFilter"] --> S6["AuthorizationFilter\n(anyRequest → authenticated)"]
        end
        subgraph C3["SecurityFilterChain @Order(3) — SecurityConfig\n/** catch-all"]
            T1["CorsFilter"] --> T2["HeaderWriterFilter"] --> T4["UsernamePasswordAuthenticationFilter\n(POST /login)"] --> T5["AnonymousAuthenticationFilter"] --> T6["AuthorizationFilter\n(anyRequest → permitAll)"]
        end
    end

    DS["DispatcherServlet"]
    CTRL["REST / SOAP Controllers"]

    Client -->|"HTTP :8080"| REDIR
    REDIR -.->|"302 to HTTPS :8443"| Client
    Client -->|"HTTPS :8443"| RLF
    RLF -.->|"HTTP 429 + Retry-After"| Client
    RLF --> DFP
    DFP -->|"oauth2 / OIDC endpoints"| C1
    DFP -->|"/api/rest/**"| C2
    DFP -->|"all other requests"| C3
    C1 --> DS
    C2 --> DS
    C3 --> DS
    DS --> CTRL
```
```mermaid
flowchart LR
    A1[A01 Broken Access Control] --> F1[AuthorizationFilter
                                        SecurityContextHolderFilter
                                        ExceptionTranslationFilter
                                        AnonymousAuthenticationFilter]
    A5[A05 Security Misconfiguration] --> F5[HeaderWriterFilter
                                              ExceptionTranslationFilter]
    A4[A04 Insecure Design] --> F4[RateLimitFilter
                                       (servlet filter, order -101, before Spring Security)]
    A7[A07 Identification and Authentication Failures] --> F7[BearerTokenAuthenticationFilter
                                        UsernamePasswordAuthenticationFilter
                                        OAuth2AuthorizationEndpointFilter
                                        OAuth2TokenEndpointFilter
                                        RateLimitFilter (login brute-force)]
```
> Note: `CsrfFilter` is intentionally absent from all three chains — all call `.csrf(AbstractHttpConfigurer::disable)`. `UsernamePasswordAuthenticationFilter` and `DefaultLoginPageGeneratingFilter` are present only on chain 3 (catch-all, via `formLogin()`), not on the Resource Server chain — this isolation is intentional: a `JSESSIONID` session cookie cannot authenticate against `/api/rest/**`. The previous design flaw (both `formLogin` and `oauth2ResourceServer` on the same chain, creating a CSRF-exposed session bypass path into the API) has been resolved by the three-chain split.

### 1b. DevSecOps

- **CI-only artifact provenance** — JAR and Docker images are built exclusively inside GitHub Actions; nothing locally-built is ever published. *(A08)*
- **Trusted dependency sources** — Maven Central / npm registry only, no unvetted repositories. *(A08)*
- **Secrets kept out of source control** — Postgres credentials are injected via `.env` (gitignored) locally and GitHub Actions `environment: ci` secrets in CI; never hardcoded in `application*.properties` (uses `${POSTGRES_PASSWORD}` placeholders).
- **Fail-fast pipeline** — unit tests gate the frontend build, Docker build/push, and integration test stages; a failure upstream stops the rest of the pipeline.
- **Dependency vulnerability scanning (Trivy)** — Job 6 (`security-scan`) in `github-actions.yml` runs three Trivy scans on every push to master: filesystem, backend Docker image, and frontend Docker image. All results upload to GitHub Code Scanning via `github/codeql-action/upload-sarif` as SARIF. Severity filter: `CRITICAL,HIGH`. Configuration in `trivy.yml` (uses local `~/.m2` cache, Gradle wrapper scan disabled). *(A06)*
- **Secret scanning in CI (gitleaks)** — `gitleaks/gitleaks-action@v2` runs in the same `security-scan` job with `fetch-depth: 0` (full history), scanning all commits for accidentally-leaked secrets.

### 1c. Cybersecurity

- **OWASP Top 10 as risk framework** — used to categorize and prioritize security work across this document, giving a consistent reference vocabulary instead of ad-hoc judgment calls. Risks currently mitigated: A01 (URL-based access rule), A02 (password hashing, opt-in TLS, no hardcoded credentials), A04 (rate limiting on write endpoints), A05 (CORS + default headers + exception handler suppressing stack traces), A06 (Trivy CVE scanning on filesystem and Docker images), A07 (OAuth2 + JWT + DB-backed auth + login rate limiting + resource server session isolation), A08 (CI-only artifact builds), A10 (no outbound calls).

> Most organizational/operational cybersecurity practices (SOC, incident response, compliance audits, threat intelligence) don't apply to a single-developer portfolio project — there's no organization to operate. The closest applicable practices are covered under 1a/1b above and their planned counterparts in 2c.

---

## Section 2: To Be Implemented

### 2a. Application Security

#### Authentication

- **Multi-Factor Authentication (MFA)** — not implemented; would layer on top of the existing OAuth2 flow. *(A07)*
#### Authorization

- **Method-level authorization (`@PreAuthorize`)** — only one role (`ADMIN`) exists today with a single URL-pattern rule; once multiple roles exist, enforce them at the method level, not just the URL matcher. *(A01)*

### 2b. DevSecOps

- **Static analysis / SAST (SonarQube or SonarCloud)** — no static analysis gate exists in the pipeline yet. *(A06, A03 — catches injection-prone patterns SQL/JPA-side, not via a Spring Security filter)*
- **SBOM generation** for built images — not currently produced.

### 2c. Cybersecurity

- **Centralized log aggregation + alerting** — no ELK (or equivalent) log shipping exists yet, and no alerting on repeated 401/403 responses. This was previously stated in this document as already implemented — it isn't; it now lives here until built. *(A09)*
- **Basic threat-modeling notes / incident-response runbook** — optional given the solo-project scope, but worth a short doc once the app has real users or real data.
