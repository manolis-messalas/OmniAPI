package com.messalas.omniapi.repository;

import com.messalas.omniapi.model.entities.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, String> {

    /**
     * Bulk-deletes all idempotency keys with a creation timestamp strictly before
     * {@code cutoff}. Runs in its own transaction; clears the persistence context
     * afterwards to prevent stale entity references.
     *
     * @param cutoff upper-exclusive time boundary; rows older than this are removed
     * @return number of rows deleted
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM IdempotencyKeyEntity e WHERE e.createdAt < :cutoff")
    int deleteByCreatedAtBefore(@Param("cutoff") Instant cutoff);
}
