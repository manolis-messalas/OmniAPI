
docker-compose up -d
 
 ./mvnw clean install

 #mvn spring-boot:run -Ph2
 #mvn spring-boot:run -Ppostgres

 ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"
 ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=postgres"
 ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=sqlite"

 ## Running the Application

### Option 1: IntelliJ IDEA (Recommended for Development)
1. Open `SpringBootDemoAApplication.java`
2. Select run configuration from dropdown:
   - `OmniAPI (H2)` - In-memory database with sample data
   - `OmniAPI (PostgreSQL)` - Docker PostgreSQL (run `docker-compose up -d` first)
   - `OmniAPI (SQLite)` - File-based database at `H:/My Projects/db/omniapidb`
3. Click Run ▶️

### Option 2: Command Line

**H2 (In-memory):**
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"
```

**PostgreSQL (requires Docker):**
```bash
# Start PostgreSQL first
docker-compose up -d

# Run application
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=postgres"
```

**SQLite (File-based):**
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=sqlite"
```

### Option 3: Package and Run JAR
```bash
./mvnw clean package
java -Dspring.profiles.active=sqlite -jar target/OmniAPI-0.0.1-SNAPSHOT.jar
```

## Database Comparison
| Profile | Database | Data Persistence | Setup Required | Sample Data |
|---------|----------|------------------|----------------|-------------|
| `h2`    | H2(in-memory)| ❌ Lost on restart | None     |✅ Included |
| `postgres`| PostgreSQL | ✅ Persists  | Docker          |❌ Empty    |
| `sqlite`| SQLite   | ✅ Persists      | None           |✅ Included |
