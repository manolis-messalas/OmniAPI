# AGENTS.md - OmniAPI Development Guide

## Project Overview

**OmniAPI** is a Spring Boot 3.5.8 multi-protocol backend serving Books and Authors data through REST, SOAP, and other transport mechanisms. Java 23 is required.

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
1. Health check: `pg_isready -U messalas -d booksdb` (checks container readiness)
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

## UI Layer (Planned)
- Framework: [React/Angular/Thymeleaf — pick one]
- Entry point: src/main/resources/static/ or separate frontend module
- API base URL: http://localhost:9090/api/rest
- Auth: JWT 

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
# Credentials: user=messalas, password=mes123, db=booksdb
```

---

## Security & Authorization

- **Framework**: Spring Security with filter chain (see `src/main/resources/Security & Authorization.md` for OWASP mapping)
- **Default Credentials**: user=`user`, password=empty (set in `application.properties`)
- **Test Profile**: `TestSecurityConfig` with `@Profile("test")` permits all requests for testing
- **CSRF**: Enabled by default; disabled in test security config for simplicity

### Key Filters (In Order)
1. `SecurityContextHolderFilter` → Load authentication
2. `AuthorizationFilter` → Check permissions (A01 Broken Access Control)
3. `CsrfFilter` → Validate CSRF tokens (A04, A05)
4. `HeaderWriterFilter` → Add security headers (A03, A05)
5. `ExceptionTranslationFilter` → Handle auth errors (A05)

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
src/main/java/com/messalas/spring_boot_demo_A/
├── SpringBootDemoAApplication.java         # Entry point
├── api/
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
├── exceptions/                             # Custom exceptions + SOAP faults
└── db/                                     # Database loaders (CommandLineRunner)

src/main/resources/
├── application*.properties                 # Profile-specific config
├── db_scripts/                             # SQL initialization (h2data.sql, sqlitedata.sql)
└── xsd/                                    # SOAP schema files (auto-gen Java code)

src/test/java/com/messalas/spring_boot_demo_A/
├── unit/                                   # Unit tests (*Test.java, @WebMvcTest/@MockBean)
├── integration/                            # Integration tests (*IT.java, @SpringBootTest, real DB)
└── DatabaseConnectionTest.java             # Profile connectivity checks
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
# Connect to psql inside the running container
docker exec -it omniapi-postgres psql -U messalas -d booksdb
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
docker exec omniapi-postgres psql -U messalas -d booksdb -c "SELECT COUNT(*) FROM author;"

# Dump database schema
docker exec omniapi-postgres pg_dump -U messalas -d booksdb --schema-only

# Check if Postgres is ready
docker exec omniapi-postgres pg_isready -U messalas -d booksdb
```

### Environment Variables
- **POSTGRES_USER**: `messalas`
- **POSTGRES_DB**: `booksdb`
- **POSTGRES_PASSWORD**: `mes123`
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
- **Security Deep Dive**: `Security & Authorization.md` (OWASP Top 10 mapping & filter chain)
- **Getting Started**: `README.md` (quick start commands for each database profile)
- **Docker Setup**: `docker-compose.yml` (PostgreSQL only—H2/SQLite don't require containers)

