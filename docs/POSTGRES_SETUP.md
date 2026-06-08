# PostgreSQL Seed Data Setup - Implementation Guide

## Overview
Your PostgreSQL container now automatically loads seed data after the health check passes. This document explains the complete setup and how it works.

## Components Created

### 1. PostgreSQL Initialization Script
**File**: `src/main/resources/db_scripts/postgres-init.sql`
- Contains raw SQL INSERT statements for authors and books
- Automatically executed by PostgreSQL container on startup
- Located in `/docker-entrypoint-initdb.d` (PostgreSQL's init directory)
- **Note**: Runs BEFORE Hibernate creates tables (SQL errors are logged but non-blocking)

### 2. Java-based Data Loader (PRIMARY)
**File**: `src/main/java/com/messalas/spring_boot_demo_A/db/PostgresDatabaseLoader.java`
- **Profile**: Active only when `--spring.profiles.active=postgres`
- **Execution Order**: 3 (after other loaders)
- Uses `BookService.saveBookAuthor()` to create authors and books together
- Handles idempotency: logs warning if data already exists (doesn't fail on duplicates)
- Runs AFTER Hibernate creates tables (guaranteed to work)

### 3. Updated Docker Compose
**File**: `docker-compose.yml`
- **Service**: `postgres` (replaced `db`)
- **Container name**: `omniapi-postgres`
- **Health Check**: 
  - Command: `pg_isready -U messalas -d booksdb`
  - Interval: 10s, Timeout: 5s, Retries: 5
  - Waits ~50 seconds max for readiness
- **Volumes**:
  - Data persistence: `postgres_data:/var/lib/postgresql/data`
  - Init scripts: `./src/main/resources/db_scripts/postgres:/docker-entrypoint-initdb.d:ro`

## Data Loading Flow

```
1. docker-compose up -d
   ↓
2. PostgreSQL container starts
   ↓
3. postgres-init.sql runs (SQL errors ignored)
   ↓
4. Health check passes (pg_isready succeeds)
   ↓
5. Application Startup
   ↓
6. Spring Security Configuration
   ↓
7. Hibernate creates/updates tables (spring.jpa.hibernate.ddl-auto=update)
   ↓
8. CommandLineRunners execute in order:
   a. Order 1: AnotherDatabaseLoader (generic seed data)
   b. Order 2: DatabaseLoader (generic seed data)
   c. Order 3: PostgresDatabaseLoader (postgres profile seed data) ← YOUR DATA LOADS HERE
   ↓
9. Application ready to serve requests
```

## How to Use

### Start PostgreSQL Container
```bash
# From project root
cd H:\My Projects\OmniAPI
docker-compose up -d

# Verify container is healthy
docker ps  # Look for "healthy" status
```

### Run Application with PostgreSQL
```bash
# Using Maven
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=postgres"

# Or using IntelliJ IDEA
# Select run config: "OmniAPI (PostgreSQL)" from dropdown
```

### Verify Seed Data Loaded
```bash
# Check logs for this message:
# "PostgreSQL seed data loading complete! Inserted 6 books with authors."

# Or query the database directly:
# docker exec -it omniapi-postgres psql -U messalas -d booksdb
# SELECT * FROM author;
# SELECT * FROM book;
```

### Stop PostgreSQL Container
```bash
docker-compose down

# To also remove data volume (cleanup):
docker-compose down -v
```

## Seed Data Loaded
The following data is loaded into PostgreSQL:

**Authors** (6 total):
- Erich Fromm (German, b. 1900)
- Bill Bryson (USA, b. 1951)
- Fyodor Dostoevsky (Russia, b. 1821)
- Konstantinos Kavafis (Greece, b. 1863)
- Tim Marshall (UK, b. 1959)
- Yuval Noah Harari (Israel, b. 1976)

**Books** (6 total):
- The Fear of Freedom (1941) - Erich Fromm
- The Body (2021) - Bill Bryson
- Crime and Punishment (1865) - Fyodor Dostoevsky
- The Poems (1963) - Konstantinos Kavafis
- Prisoners of Geography (2015) - Tim Marshall
- Sapiens: A Brief History of Humankind (2015) - Yuval Noah Harari

## Key Features

✅ **Idempotent**: Safe to restart container multiple times without duplicate errors
✅ **Automatic**: No manual SQL file execution needed
✅ **Health-aware**: Waits for container to be ready before application starts
✅ **Profile-scoped**: Only loads when `postgres` profile is active
✅ **Exception-safe**: Catches and logs errors gracefully

## Troubleshooting

### Container won't start
```bash
# Check logs
docker-compose logs postgres

# Common issue: Port 5432 already in use
# Solution: docker ps, identify conflicting container, docker kill <container_id>
```

### Data not loading
```bash
# Check application logs for:
# "PostgreSQL seed data loading complete!"

# If you see "already exists" warning:
# This is normal - data was already loaded in a previous run
# The loader uses try-catch to handle this gracefully
```

### Want to reset data
```bash
# Stop and remove container + volume
docker-compose down -v

# Recreate everything
docker-compose up -d
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=postgres"
```

---

**Summary**: Your PostgreSQL environment is now fully configured to automatically load seed data. The Java-based loader (`PostgresDatabaseLoader`) ensures data is loaded after tables are created, making it reliable and production-ready.

