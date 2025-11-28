package com.messalas.spring_boot_demo_A.unit;

import com.messalas.spring_boot_demo_A.model.builders.BookAuthorDTOBuilder;
import com.messalas.spring_boot_demo_A.model.dto.AuthorDTO;
import com.messalas.spring_boot_demo_A.model.dto.BookAuthorDTO;
import com.messalas.spring_boot_demo_A.model.dto.BookDTO;
import com.messalas.spring_boot_demo_A.model.entities.AuthorEntity;
import com.messalas.spring_boot_demo_A.model.entities.BookEntity;
import com.messalas.spring_boot_demo_A.repository.AuthorRepository;
import com.messalas.spring_boot_demo_A.repository.BookRepository;
import com.messalas.spring_boot_demo_A.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceTest.class);

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private BookService bookService;

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
    public void testSaveBookAuthor_Success() {
        BookAuthorDTO bookAuthorDTO = new BookAuthorDTOBuilder()
                .bookName("Test Book")
                .publicationYear("2024")
                .authorName("Test Author")
                .dateOfBirth("1 Jan 1980")
                .countryOfOrigin("USA")
                .build();

        AuthorEntity savedAuthor = new AuthorEntity();
        savedAuthor.setId(1L);
        savedAuthor.setName("Test Author");
        savedAuthor.setDateOfBirth("1 Jan 1980");
        savedAuthor.setCountryOfOrigin("USA");

        BookEntity savedBook = new BookEntity();
        savedBook.setId(1L);
        savedBook.setName("Test Book");
        savedBook.setPublicationYear("2024");
        savedBook.setAuthorEntity(savedAuthor);

        logger.info("Starting testSaveBookAuthor_Success");

        when(authorRepository.save(any(AuthorEntity.class))).thenReturn(savedAuthor);
        when(bookRepository.save(any(BookEntity.class))).thenReturn(savedBook);

        Long bookId = bookService.saveBookAuthor(bookAuthorDTO);

        assertNotNull(bookId);
        assertEquals(1L, bookId);
        verify(authorRepository).save(any(AuthorEntity.class));
        verify(bookRepository).save(any(BookEntity.class));

        logger.info("testSaveBookAuthor_Success completed successfully with book ID: {}", bookId);
    }

    @Test
    public void testSaveBook_Success() {
        AuthorDTO authorDTO = AuthorDTO.builder()
                .authorId(1L)
                .authorName("Test Author")
                .dateOfBirth("1 Jan 1980")
                .countryOfOrigin("USA")
                .build();

        BookDTO bookDTO = BookDTO.builder()
                .bookName("Test Book")
                .publicationYear("2024")
                .authorDTO(authorDTO)
                .build();

        AuthorEntity authorEntity = new AuthorEntity();
        authorEntity.setId(1L);
        authorEntity.setName("Test Author");

        BookEntity savedBook = new BookEntity();
        savedBook.setId(1L);
        savedBook.setName("Test Book");
        savedBook.setPublicationYear("2024");
        savedBook.setAuthorEntity(authorEntity);

        logger.info("Starting testSaveBook_Success");

        when(authorRepository.findByName("Test Author")).thenReturn(Optional.of(authorEntity));
        when(bookRepository.save(any(BookEntity.class))).thenReturn(savedBook);

        Long bookId = bookService.saveBook(bookDTO);

        assertNotNull(bookId);
        assertEquals(1L, bookId);
        verify(authorRepository).findByName("Test Author");
        verify(bookRepository).save(any(BookEntity.class));

        logger.info("testSaveBook_Success completed successfully with book ID: {}", bookId);
    }

    @Test
    public void testSaveBook_AuthorNotFound() {
        AuthorDTO authorDTO = AuthorDTO.builder()
                .authorName("Non-existent Author")
                .build();

        BookDTO bookDTO = BookDTO.builder()
                .bookName("Test Book")
                .publicationYear("2024")
                .authorDTO(authorDTO)
                .build();

        logger.info("Starting testSaveBook_AuthorNotFound");

        when(authorRepository.findByName("Non-existent Author")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.saveBook(bookDTO)
        );

        assertEquals("Author not found with name: Non-existent Author", exception.getMessage());
        verify(authorRepository).findByName("Non-existent Author");
        verify(bookRepository, never()).save(any());

        logger.info("testSaveBook_AuthorNotFound completed successfully, exception thrown as expected");
    }

    @Test
    public void testGetAllBooks() {
        AuthorEntity author = new AuthorEntity();
        author.setId(1L);
        author.setName("Test Author");

        BookEntity book1 = new BookEntity();
        book1.setId(1L);
        book1.setName("Book 1");
        book1.setPublicationYear("2023");
        book1.setAuthorEntity(author);

        BookEntity book2 = new BookEntity();
        book2.setId(2L);
        book2.setName("Book 2");
        book2.setPublicationYear("2024");
        book2.setAuthorEntity(author);

        List<BookEntity> bookEntities = Arrays.asList(book1, book2);

        logger.info("Starting testGetAllBooks");

        when(bookRepository.findAll()).thenReturn(bookEntities);

        List<BookDTO> result = bookService.getAllBooks();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Book 1", result.get(0).getBookName());
        assertEquals("Book 2", result.get(1).getBookName());
        verify(bookRepository).findAll();

        logger.info("testGetAllBooks completed successfully with {} books", result.size());
    }

    @Test
    public void testFindBookByName_Success() {
        String bookName = "Test Book";

        AuthorEntity author = new AuthorEntity();
        author.setId(1L);
        author.setName("Test Author");

        BookEntity bookEntity = new BookEntity();
        bookEntity.setId(1L);
        bookEntity.setName(bookName);
        bookEntity.setPublicationYear("2024");
        bookEntity.setAuthorEntity(author);

        logger.info("Starting testFindBookByName_Success with name: {}", bookName);

        when(bookRepository.findByName(bookName)).thenReturn(Optional.of(bookEntity));

        BookDTO result = bookService.findBookByName(bookName);

        assertNotNull(result);
        assertEquals(bookName, result.getBookName());
        assertEquals("2024", result.getPublicationYear());
        verify(bookRepository).findByName(bookName);

        logger.info("testFindBookByName_Success completed successfully");
    }

    @Test
    public void testFindBookByName_NotFound() {
        String bookName = "Non-existent Book";

        logger.info("Starting testFindBookByName_NotFound with name: {}", bookName);

        when(bookRepository.findByName(bookName)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.findBookByName(bookName)
        );

        assertEquals("Book not found with name: Non-existent Book", exception.getMessage());
        verify(bookRepository).findByName(bookName);

        logger.info("testFindBookByName_NotFound completed successfully, exception thrown as expected");
    }

    @Test
    public void testDeleteBook_Success() {
        Long bookId = 1L;

        logger.info("Starting testDeleteBook_Success with ID: {}", bookId);

        when(bookRepository.existsById(bookId)).thenReturn(true);

        bookService.deleteBook(bookId);

        verify(bookRepository).existsById(bookId);
        verify(bookRepository).deleteById(bookId);

        logger.info("testDeleteBook_Success completed successfully");
    }

    @Test
    public void testDeleteBook_NotFound() {
        Long bookId = 999L;

        logger.info("Starting testDeleteBook_NotFound with ID: {}", bookId);

        when(bookRepository.existsById(bookId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.deleteBook(bookId)
        );

        assertEquals("Book with id 999 not found", exception.getMessage());
        verify(bookRepository).existsById(bookId);
        verify(bookRepository, never()).deleteById(any());

        logger.info("testDeleteBook_NotFound completed successfully, exception thrown as expected");
    }
}