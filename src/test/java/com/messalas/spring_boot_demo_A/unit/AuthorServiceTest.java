package com.messalas.spring_boot_demo_A.unit;

import com.messalas.spring_boot_demo_A.model.dto.AuthorDTO;
import com.messalas.spring_boot_demo_A.model.entities.AuthorEntity;
import com.messalas.spring_boot_demo_A.repository.AuthorRepository;
import com.messalas.spring_boot_demo_A.repository.BookRepository;
import com.messalas.spring_boot_demo_A.service.AuthorService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthorServiceTest.class);

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private AuthorService authorService;

    private final boolean testPassed = true;

    @AfterEach
    public void logTestResult() {
        if (testPassed) {
            System.out.println("Test passed successfully!");
            logger.info("Test passed successfully!");
        } else {
            System.out.println("Test failed!");
            logger.error("Test failed!");
        }
    }

    @Test
    public void testCreateAuthor() {
        AuthorDTO authorDTO = AuthorDTO.builder()
                .authorName("Test Author")
                .dateOfBirth("1 Jan 1980")
                .countryOfOrigin("USA")
                .build();

        AuthorEntity savedEntity = new AuthorEntity();
        savedEntity.setId(1L);
        savedEntity.setName("Test Author");
        savedEntity.setDateOfBirth("1 Jan 1980");
        savedEntity.setCountryOfOrigin("USA");

        logger.info("Starting testCreateAuthor");

        when(authorRepository.save(any(AuthorEntity.class))).thenReturn(savedEntity);

        Long authorId = authorService.createAuthor(authorDTO);

        assertNotNull(authorId);
        assertEquals(1L, authorId);
        verify(authorRepository).save(any(AuthorEntity.class));

        logger.info("testCreateAuthor completed successfully with ID: {}", authorId);
    }

    @Test
    public void testGetAllAuthors() {
        AuthorEntity author1 = new AuthorEntity();
        author1.setId(1L);
        author1.setName("Author 1");
        author1.setDateOfBirth("1 Jan 1980");
        author1.setCountryOfOrigin("USA");

        AuthorEntity author2 = new AuthorEntity();
        author2.setId(2L);
        author2.setName("Author 2");
        author2.setDateOfBirth("15 Feb 1975");
        author2.setCountryOfOrigin("UK");

        List<AuthorEntity> authorEntities = Arrays.asList(author1, author2);

        logger.info("Starting testGetAllAuthors");

        when(authorRepository.findAll()).thenReturn(authorEntities);

        List<AuthorDTO> result = authorService.getAllAuthors();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Author 1", result.get(0).getAuthorName());
        assertEquals("Author 2", result.get(1).getAuthorName());
        verify(authorRepository).findAll();

        logger.info("testGetAllAuthors completed successfully with {} authors", result.size());
    }

    @Test
    public void testGetAuthorById_Success() {
        Long authorId = 1L;
        AuthorEntity authorEntity = new AuthorEntity();
        authorEntity.setId(authorId);
        authorEntity.setName("Test Author");
        authorEntity.setDateOfBirth("1 Jan 1980");
        authorEntity.setCountryOfOrigin("USA");

        logger.info("Starting testGetAuthorById_Success with ID: {}", authorId);

        OngoingStubbing<Optional<AuthorEntity>> optionalOngoingStubbing = when(authorRepository.findById(authorId)).thenReturn(Optional.of(authorEntity));

        AuthorDTO result = authorService.getAuthorById(authorId);

        assertNotNull(result);
        assertEquals(authorId, result.getAuthorId());
        assertEquals("Test Author", result.getAuthorName());
        assertEquals("USA", result.getCountryOfOrigin());
        verify(authorRepository).findById(authorId);

        logger.info("testGetAuthorById_Success completed successfully");
    }

    @Test
    public void testGetAuthorById_NotFound() {
        Long authorId = 999L;

        logger.info("Starting testGetAuthorById_NotFound with ID: {}", authorId);

        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authorService.getAuthorById(authorId)
        );

        assertEquals("Author not found with ID: 999", exception.getMessage());
        verify(authorRepository).findById(authorId);

        logger.info("testGetAuthorById_NotFound completed successfully, exception thrown as expected");
    }

    @Test
    public void testDeleteAuthor_Success() {
        Long authorId = 1L;
        AuthorEntity authorEntity = new AuthorEntity();
        authorEntity.setId(authorId);
        authorEntity.setName("Test Author");

        logger.info("Starting testDeleteAuthor_Success with ID: {}", authorId);

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(authorEntity));
        when(bookRepository.existsByAuthorEntityId(authorId)).thenReturn(false);

        authorService.deleteAuthor(authorId);

        verify(authorRepository).findById(authorId);
        verify(bookRepository).existsByAuthorEntityId(authorId);
        verify(authorRepository).delete(authorEntity);

        logger.info("testDeleteAuthor_Success completed successfully");
    }

    @Test
    public void testDeleteAuthor_NotFound() {
        Long authorId = 999L;

        logger.info("Starting testDeleteAuthor_NotFound with ID: {}", authorId);

        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> authorService.deleteAuthor(authorId)
        );

        assertEquals("Author not found", exception.getMessage());
        verify(authorRepository).findById(authorId);
        verify(bookRepository, never()).existsByAuthorEntityId(any());
        verify(authorRepository, never()).delete(any());

        logger.info("testDeleteAuthor_NotFound completed successfully, exception thrown as expected");
    }

    @Test
    public void testDeleteAuthor_HasBooks() {
        Long authorId = 1L;
        AuthorEntity authorEntity = new AuthorEntity();
        authorEntity.setId(authorId);
        authorEntity.setName("Test Author");

        logger.info("Starting testDeleteAuthor_HasBooks with ID: {}", authorId);

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(authorEntity));
        when(bookRepository.existsByAuthorEntityId(authorId)).thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authorService.deleteAuthor(authorId)
        );

        assertEquals("Cannot delete author because books reference this author", exception.getMessage());
        verify(authorRepository).findById(authorId);
        verify(bookRepository).existsByAuthorEntityId(authorId);
        verify(authorRepository, never()).delete(any());

        logger.info("testDeleteAuthor_HasBooks completed successfully, exception thrown as expected");
    }
}