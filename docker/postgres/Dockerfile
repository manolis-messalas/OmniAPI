FROM postgres:latest

# Copy PostgreSQL init scripts into the image so they run on first startup
COPY src/main/resources/db_scripts/postgres/*.sql /docker-entrypoint-initdb.d/

