package com.messalas.omniapi.unit;

import com.messalas.omniapi.exceptions.DuplicateRequestException;
import com.messalas.omniapi.model.entities.IdempotencyKeyEntity;
import com.messalas.omniapi.repository.IdempotencyKeyRepository;
import com.messalas.omniapi.service.IdempotencyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IdempotencyServiceTest {

    @InjectMocks
    private IdempotencyService idempotencyService;

    @Mock
    private IdempotencyKeyRepository repository;

    @Test
    public void registerKey_newKey_savesSuccessfully() {
        String key = "unique-key-123";
        when(repository.saveAndFlush(any())).thenReturn(new IdempotencyKeyEntity(key));

        assertDoesNotThrow(() -> idempotencyService.registerKey(key));

        verify(repository).saveAndFlush(any(IdempotencyKeyEntity.class));
    }

    @Test
    public void registerKey_duplicateKey_throwsDuplicateRequestException() {
        String key = "duplicate-key-456"; // gitleaks:allow
        when(repository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("unique constraint violation"));

        DuplicateRequestException ex = assertThrows(
                DuplicateRequestException.class,
                () -> idempotencyService.registerKey(key));

        assertTrue(ex.getMessage().contains(key));
    }

    @Test
    public void deleteKey_existingKey_deletesFromRepository() {
        String key = "key-to-delete";

        assertDoesNotThrow(() -> idempotencyService.deleteKey(key));

        verify(repository).deleteById(key);
    }
}
