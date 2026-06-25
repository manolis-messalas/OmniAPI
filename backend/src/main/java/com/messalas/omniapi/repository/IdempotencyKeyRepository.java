package com.messalas.omniapi.repository;

import com.messalas.omniapi.model.entities.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, String> {
}
