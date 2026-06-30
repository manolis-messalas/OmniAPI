package com.messalas.omniapi.unit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.messalas.omniapi.exceptions.DuplicateRequestException;
import com.messalas.omniapi.model.entities.IdempotencyKeyEntity;
import com.messalas.omniapi.repository.IdempotencyKeyRepository;
import com.messalas.omniapi.service.IdempotencyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

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

    @Test
    public void cleanupStaleKeys_callsRepositoryWithOneDayCutoffAndLogsSuccess() {
        when(repository.deleteByCreatedAtBefore(any(Instant.class))).thenReturn(5);

        Logger serviceLogger = (Logger) LoggerFactory.getLogger(IdempotencyService.class);
        ListAppender<ILoggingEvent> logCapture = new ListAppender<>();
        logCapture.start();
        serviceLogger.addAppender(logCapture);

        try {
            Instant beforeCall = Instant.now().minus(Duration.ofDays(1));
            idempotencyService.cleanupStaleKeys();
            Instant afterCall = Instant.now().minus(Duration.ofDays(1));

            ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
            verify(repository, times(1)).deleteByCreatedAtBefore(cutoffCaptor.capture());

            Instant capturedCutoff = cutoffCaptor.getValue();
            assertFalse(capturedCutoff.isBefore(beforeCall),
                    "Cutoff must be at or after (now - 1 day) measured before the call");
            assertFalse(capturedCutoff.isAfter(afterCall),
                    "Cutoff must be at or before (now - 1 day) measured after the call");

            List<ILoggingEvent> logs = logCapture.list;
            assertTrue(logs.stream().anyMatch(e ->
                            e.getLevel().equals(Level.INFO) &&
                            e.getFormattedMessage().contains("Starting idempotency key cleanup")),
                    "Expected INFO log: 'Starting idempotency key cleanup'");
            assertTrue(logs.stream().anyMatch(e ->
                            e.getLevel().equals(Level.INFO) &&
                            e.getFormattedMessage().contains("Deleted 5 stale keys")),
                    "Expected INFO log containing 'Deleted 5 stale keys'");
        } finally {
            serviceLogger.detachAppender(logCapture);
        }
    }

    @Test
    public void cleanupStaleKeys_onRepositoryException_logsErrorAndDoesNotRethrow() {
        when(repository.deleteByCreatedAtBefore(any(Instant.class)))
                .thenThrow(new RuntimeException("DB connection lost"));

        Logger serviceLogger = (Logger) LoggerFactory.getLogger(IdempotencyService.class);
        ListAppender<ILoggingEvent> logCapture = new ListAppender<>();
        logCapture.start();
        serviceLogger.addAppender(logCapture);

        try {
            assertDoesNotThrow(() -> idempotencyService.cleanupStaleKeys(),
                    "Cleanup exceptions must not propagate to the caller");

            assertTrue(logCapture.list.stream().anyMatch(e ->
                            e.getLevel().equals(Level.ERROR) &&
                            e.getFormattedMessage().contains("Idempotency key cleanup failed")),
                    "Expected ERROR log: 'Idempotency key cleanup failed'");
        } finally {
            serviceLogger.detachAppender(logCapture);
        }
    }
}
