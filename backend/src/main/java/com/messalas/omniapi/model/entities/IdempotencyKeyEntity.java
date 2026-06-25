package com.messalas.omniapi.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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

    // True for brand-new instances (forces persist/INSERT); flipped to false by
    // @PostLoad so that deleteById can remove DB-loaded entities normally.
    @Transient
    private boolean isNew = true;

    public IdempotencyKeyEntity(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
        this.createdAt = Instant.now();
    }

    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    @JsonIgnore
    public String getId() {
        return idempotencyKey;
    }

    @Override
    @JsonIgnore
    public boolean isNew() {
        return isNew;
    }
}
