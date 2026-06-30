package com.messalas.omniapi.integration;

import com.messalas.omniapi.model.entities.IdempotencyKeyEntity;
import com.messalas.omniapi.repository.IdempotencyKeyRepository;
import com.messalas.omniapi.service.IdempotencyService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for idempotency-key TTL cleanup.
 *
 * <p>Runs against the H2 in-memory database by default. The same tests can be run
 * against PostgreSQL or SQLite by activating the corresponding profile:
 * <pre>
 *   ./mvnw verify -Dspring.profiles.active=postgres   # requires docker-compose up -d
 *   ./mvnw verify -Dspring.profiles.active=sqlite
 * </pre>
 */
@SpringBootTest
@ActiveProfiles("h2")
public class IdempotencyCleanupIT {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyCleanupIT.class);

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private IdempotencyKeyRepository repository;

    /**
     * Inserts 3 stale keys (2 days old) and 2 fresh keys, then triggers cleanup and
     * asserts that exactly the 3 stale rows are removed while the 2 fresh rows survive.
     */
    @Test
    public void cleanupStaleKeys_deletesOnlyExpiredKeys() {
        long countBefore = repository.count();

        // 3 stale keys — created_at is 2 days ago, well beyond the 1-day TTL
        for (int i = 0; i < 3; i++) {
            IdempotencyKeyEntity stale = new IdempotencyKeyEntity();
            stale.setIdempotencyKey(UUID.randomUUID().toString());
            stale.setCreatedAt(Instant.now().minus(Duration.ofDays(2)));
            repository.save(stale);
        }

        // 2 fresh keys — created just now, must survive cleanup
        for (int i = 0; i < 2; i++) {
            repository.save(new IdempotencyKeyEntity(UUID.randomUUID().toString()));
        }

        long countAfterInsert = repository.count();
        assertEquals(countBefore + 5, countAfterInsert, "Setup: expected 5 rows inserted");

        idempotencyService.cleanupStaleKeys();

        long countAfterCleanup = repository.count();
        assertEquals(countAfterInsert - 3, countAfterCleanup,
                "Cleanup must delete exactly 3 stale keys and preserve 2 fresh keys");

        logger.info("cleanupStaleKeys_deletesOnlyExpiredKeys passed");
    }
}
