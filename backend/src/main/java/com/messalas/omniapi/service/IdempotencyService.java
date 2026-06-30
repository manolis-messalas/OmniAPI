package com.messalas.omniapi.service;

import com.messalas.omniapi.exceptions.DuplicateRequestException;
import com.messalas.omniapi.model.entities.IdempotencyKeyEntity;
import com.messalas.omniapi.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyKeyRepository repository;

    /**
     * Atomically registers a new idempotency key. Throws DuplicateRequestException
     * if the key already exists (concurrent or repeated request). Runs in its own
     * REQUIRES_NEW transaction so the key commits before business logic begins.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerKey(String key) {
        try {
            repository.saveAndFlush(new IdempotencyKeyEntity(key));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateRequestException(
                    "Duplicate request: Idempotency-Key '" + key + "' was already processed");
        }
    }

    /**
     * Removes the key when the business operation fails, so the client can safely
     * retry with the same key. Runs in its own REQUIRES_NEW transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteKey(String key) {
        repository.deleteById(key);
    }

    /**
     * Scheduled housekeeping job: bulk-deletes idempotency keys older than 24 hours.
     * Runs 1 minute after startup, then every hour after the previous run completes.
     * Errors are logged but never rethrown — a failed cleanup does not affect request handling.
     */
    @Scheduled(fixedDelay = 3_600_000, initialDelay = 60_000)
    public void cleanupStaleKeys() {
        log.info("Starting idempotency key cleanup");
        long startTime = System.currentTimeMillis();
        try {
            Instant cutoff = Instant.now().minus(Duration.ofDays(1));
            int deleted = repository.deleteByCreatedAtBefore(cutoff);
            log.info("Deleted {} stale keys in {}ms", deleted, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Idempotency key cleanup failed", e);
        }
    }
}
