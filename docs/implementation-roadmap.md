# OmniAPI — Implementation Roadmap

> **Scope note:** this file describes what is **planned/aspirational** — not yet verified as implemented. For the current, verified state of the codebase, see [`AGENTS.md`](AGENTS.md). When an item here is actually built, move its description there and drop it from this file (or mark ✅ only after confirming against source, not from memory).

OmniAPI's real thesis: one domain (books/authors, deliberately boring) exposed through *every* major API paradigm, built correctly enough to serve as the base for future projects. Below is prioritized by how strongly each item signals seniority and how directly it advances that thesis. ✅ = already evidenced in repo. 🔲 = gap to close.

## Tier 0 — Protocol Completeness (the actual differentiator)

This is the project's core thesis — lead interviews with this, not with CRUD details.

**0a. REST** ✅ — `api/rest`, standard resource endpoints over the book/author domain.

**0b. SOAP** ✅ (rare, strong signal) — `api/soap`, `xsd/`, `wsdl4j`, `spring-ws-core`. Most candidates have never touched WSDL/XSD; you have a contract-first SOAP service reusing the same service layer as REST.

**0c. WebSockets** 🔲
- *Signal:* Full-duplex, stateful connections vs request/response — and knowing the operational cost (sticky sessions, scaling across pods).
- *Layer:* Backend / Architecture
- *Proof point:* `spring-boot-starter-websocket` + STOMP, broadcasting a "book added" event to subscribed clients. Discuss scaling WS across multiple replicas via a Redis pub-sub backplane (ties to Tier 3).

**0d. GraphQL** 🔲
- *Signal:* Understanding over-/under-fetching tradeoffs vs REST, and the N+1 problem unique to graph resolvers.
- *Layer:* Backend
- *Proof point:* Spring for GraphQL, schema-first `schema.graphqls` for Book/Author, resolving via the existing service layer, fixing N+1 with `@BatchMapping`.

**0e. gRPC** 🔲
- *Signal:* Binary contracts, HTTP/2 multiplexing, service-to-service use case vs public-facing REST.
- *Layer:* Backend / Infra
- *Proof point:* Protobuf-defined `BookService`/`AuthorService`, one unary RPC and one server-streaming RPC (e.g., `streamAllBooks`). Explain why you'd pick gRPC for internal calls and REST for browser/public clients — the project can demo both side by side.

**The unifying argument:** all five protocols sit in front of the *same* service layer — zero duplicated business logic. That reuse story is worth more than any single protocol.

## Tier 1 — Performance & Search

**1a. Caching** 🔲
- *Signal:* Cache-aside pattern, invalidation correctness (the hard part, not the lookup), TTL-based eviction.
- *Layer:* Backend / Infra
- *Proof point:* `@Cacheable` on author/book lookups backed by Redis; `@CacheEvict` on writes. Add `ETag`/`Cache-Control` headers on REST responses for HTTP-level caching. Discuss TTL tuning and the stale-read tradeoff.

**1b. Search via Elasticsearch** 🔲
- *Signal:* Recognizing SQL `LIKE` doesn't scale for relevance ranking, fuzzy match, or faceted search.
- *Layer:* Backend / Infra
- *Proof point:* Elasticsearch index populated on write via Spring Data Elasticsearch; `/search/books?q=` endpoint backed by ES instead of Postgres. Talking point: why you'd pick ES for search and keep Postgres as the source of truth.

## Tier 2 — Cloud & Kubernetes

**2a. Kubernetes manifests** 🔲
- *Signal:* Knowing the difference between "runs in Docker" and "runs correctly under an orchestrator" — probes, config separation, statefulness.
- *Layer:* Infra
- *Proof point:* `/k8s` directory with Deployment/Service manifests for backend, frontend, and a StatefulSet (or managed-DB stand-in) for Postgres. Wire `livenessProbe`/`readinessProbe` to the Actuator health endpoint you already expose. Use a ConfigMap/Secret split instead of baked-in env vars — discuss HPA scaling triggers (CPU vs custom metrics) even if not implemented.

**2b. Cloud deployment** 🔲
- *Signal:* IaC discipline, managed-service tradeoffs, cost/least-privilege awareness — not just "it's in a container."
- *Layer:* Infra
- *Proof point:* Terraform for one cloud (pick AWS or Azure to match target employers) provisioning a managed Postgres instance + container hosting (ECS/AKS) instead of self-hosting the DB container. Talking point: why managed Postgres beats your current containerized one in prod (backups, failover, patching) even though the container version is fine for portfolio/dev.

## Tier 3 — Quality Gates

**3. SonarQube / static analysis in CI** 🔲
- *Signal:* Distinguishing SAST/code-quality gating (SonarQube) from dependency scanning (Trivy/Snyk) — and treating both as CI gates, not optional reports.
- *Layer:* Infra
- *Proof point:* Add a SonarCloud job to `github-actions.yml` (free tier), require its quality gate to pass before merge. Pair with a Trivy/grype image scan using the `security-events: write` permission already declared in the workflow.

## Tier 4 — Principles (talking points mapped to real files, not recited acronyms)

**4a. ACID** ✅
- *Layer:* Architecture / Backend
- *Proof point:* `@Transactional` boundary when creating a book and linking its author atomically. Pair with the implemented optimistic locking (`@Version` on `BookEntity`/`AuthorEntity`, `ObjectOptimisticLockingFailureException` → 409 Conflict) to demonstrate isolation in practice, not just the acronym — see AGENTS.md for the full pattern.

**4b. CAP** 🔲 (partial — Postgres CP side exists; requires Tier 1b Elasticsearch for the AP tradeoff)
- *Layer:* Architecture
- *Proof point:* Once 1b (Elasticsearch) lands, you have a real CAP story: Postgres is your CP source of truth, Elasticsearch is your AP read replica that can lag. Explain explicitly that you chose availability/staleness for search and consistency for transactional writes — a deliberate tradeoff, not an accident.

**4c. SOLID — mapped to actual code, not the acronym**
- *Layer:* Backend
- SRP ✅ — `service`/`mapper`/`builder` split means no class does mapping and persistence and business logic.
- LSP ✅ — H2/Postgres/SQLite are interchangeable via a profile switch; no code changes required.
- ISP ✅ — separate `BookRepository`/`AuthorRepository` instead of one god-repository.
- DIP ✅ — services depend on repository *interfaces*, constructor-injected, never on `Hibernate` directly.

## Carryover — Already Strong, Keep These In Your Back Pocket

- **Layered architecture** ✅ — controller → service → repository, DTOs at the edges.
- **MapStruct over reflection-based mapping** ✅ — compile-time, type-safe.
- **Builder pattern** ✅ — `model/builders` for complex entity construction.
- **Multi-dialect persistence** ✅ — H2 (fast unit tests) vs Postgres (high-fidelity integration tests in CI), explicit testing-pyramid reasoning.
- **Surefire/Failsafe split + JaCoCo** ✅ — unit vs integration test phases, coverage as a CI artifact, not vanity metric.
- **Staged CI pipeline with artifact handoff** ✅ — unit-tests → frontend-build → docker-build-push → integration-tests, fail-fast ordering, GHCR images per concern (backend/frontend/postgres).
- **Stateless auth via Spring Security** ✅ — `SecurityFilterChain`, method-level `@PreAuthorize`, locked-down Actuator surface.
- **Centralized exception handling** 🔲 — upgrade the existing `exceptions` package to `@RestControllerAdvice` returning `ProblemDetail` (RFC 7807) for a consistent error contract across REST *and* a SOAP fault-mapping equivalent.

**Idempotency keys** ✅
- Implemented for all non-idempotent POST endpoints (`/createAuthor`, `/addBook`, `/addBookAuthor`) and their SOAP equivalents (`CreateAuthorRequest`, `CreateBookRequest`, `CreateBookAuthorRequest`).
- REST: `Idempotency-Key: <UUID>` header required; missing → 400, duplicate → 409 Conflict.
- SOAP: `idempotencyKey` element in XSD; missing → `AuthorValidationException`/`BookValidationException` → `FaultCode.CLIENT` fault; duplicate → `DuplicateRequestException` → `FaultCode.CLIENT` fault.
- `idempotency_keys` table (PK constraint) provides atomic duplicate detection via `saveAndFlush` + catching `DataIntegrityViolationException` — no `existsById + save` race.
- On business-logic failure the key is deleted in a `REQUIRES_NEW` transaction so the client can safely retry with the same key.
- Frontend generates one UUID per form open (`useState(() => crypto.randomUUID())`); same key reused for retries.
- *Possible extension:* response-replay caching in Redis; TTL-based expiry of old keys.

**Optimistic locking** ✅
- `@Version Long version` on `BookEntity` and `AuthorEntity`; `version` in both DTOs and in the `<Book>`/`<Author>` XSD elements.
- Service rejects updates missing `version` (400), sets the client-supplied version on the entity before `saveAndFlush()`, catches `ObjectOptimisticLockingFailureException` → `OptimisticLockConflictException`.
- REST: 409 Conflict via `RESTExceptionHandler`; successful PUT returns the saved DTO with the new incremented version.
- SOAP: `UpdateBookRequest`/`UpdateAuthorRequest` operations added to both SOAP controllers; conflict surfaces as `@SoapFault(faultCode = FaultCode.CLIENT)`.

**Soft delete** 🔲
- *Signal:* Data durability and audit trail without hard deletes; knowing the pitfalls (unique-constraint collisions, leaking deleted rows into queries, cascade behaviour) separates a senior from a junior.
- *Layer:* Backend / Data
- *Proof point:* Add `deleted_at TIMESTAMP` (nullable) to `Book`/`Author`. Annotate entities with `@SQLRestriction("deleted_at IS NULL")` (Hibernate 6) so all queries automatically exclude deleted rows without touching every repository method. The `delete` service method sets `deleted_at = now()` rather than issuing a `DELETE`. Add a privileged `/admin/books/{id}/restore` endpoint that nulls `deleted_at`. Discuss the unique-constraint collision problem (e.g., ISBN uniqueness) and one mitigation: a partial index (`UNIQUE (isbn) WHERE deleted_at IS NULL`) or folding `deleted_at` into the unique key. SOAP equivalent: `SoftDeleteBookRequest`/`RestoreBookRequest` operations in the WSDL.

## Build Order

1. **Tier 0** protocols — this is what makes OmniAPI distinct; do it before anything else.
2. **Tier 1** caching + search — performance and query improvements on top of the completed protocol layer.
3. **Tier 2** K8s/cloud — promote the now-feature-complete app to a real deployment target.
4. **Tier 3 & 4** — quality gates and principle talking points apply throughout; revisit them as each tier lands rather than batching at the end.
