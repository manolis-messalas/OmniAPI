package com.messalas.omniapi.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.time.Instant;

@Entity
@Table(name = "idempotency_keys")
@Data
@NoArgsConstructor
public class IdempotencyKeyEntity implements Persistable<String> {

    @Id
    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;

    @Column(name = "request_hash", length = 128)
    private String requestHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public IdempotencyKeyEntity(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
        this.createdAt = Instant.now();
    }

    @Override
    @JsonIgnore
    public String getId() {
        return idempotencyKey;
    }

    // Always true so Spring Data uses persist() (INSERT) instead of merge() (upsert),
    // ensuring DataIntegrityViolationException is thrown on duplicate keys.
    @Override
    @JsonIgnore
    public boolean isNew() {
        return true;
    }
}
