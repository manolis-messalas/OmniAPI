package com.messalas.omniapi.service;

import com.messalas.omniapi.exceptions.DuplicateRequestException;
import com.messalas.omniapi.model.entities.IdempotencyKeyEntity;
import com.messalas.omniapi.repository.IdempotencyKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {

    @Autowired
    private IdempotencyKeyRepository repository;

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
}
