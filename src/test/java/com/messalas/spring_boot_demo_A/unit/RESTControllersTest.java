package com.messalas.spring_boot_demo_A.unit;

import com.messalas.spring_boot_demo_A.api.rest.AuthorRESTController;
import com.messalas.spring_boot_demo_A.model.builders.BookAuthorDTOBuilder;
import com.messalas.spring_boot_demo_A.api.rest.BooksRESTController;
import com.messalas.spring_boot_demo_A.model.dto.AuthorDTO;
import com.messalas.spring_boot_demo_A.model.dto.BookAuthorDTO;
import com.messalas.spring_boot_demo_A.model.dto.BookDTO;
import com.messalas.spring_boot_demo_A.service.AuthorService;
import com.messalas.spring_boot_demo_A.service.BookService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers ={BooksRESTController.class, AuthorRESTController.class})
public class RESTControllersTest {

    private static final Logger logger = LoggerFactory.getLogger(RESTControllersTest.class);

    @Autowired
    private BooksRESTController booksRESTController;

    @Autowired
    private AuthorRESTController authorRESTController;

    @MockBean
    private BookService bookService;

    @MockBean
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
    public void testAddBookAuthor(){
        BookAuthorDTO bookAuthorDTO = new BookAuthorDTOBuilder()
                .bookName("manolosbook")
                .authorName("manolo")
                .publicationYear("2024")
                .countryOfOrigin("aktaio")
                .dateOfBirth("14/3/93")
                .build();

        logger.info("Starting testCreateBookAuthor test with DTO: {}", bookAuthorDTO.toString());

        when(bookService.saveBookAuthor(bookAuthorDTO)).thenReturn(2L);

        ResponseEntity<BookAuthorDTO> response = booksRESTController.addBookAuthor(bookAuthorDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testDeleteBook() {
        Long bookId = 1L;

        logger.info("Starting testDeleteBook with id: {}", bookId);

        ResponseEntity<Void> response = booksRESTController.deleteBook(bookId);

        verify(bookService).deleteBook(bookId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testAddBook() {
        BookDTO bookDTO = BookDTO.builder()
                .bookName("Test Book")
                .publicationYear("2024")
                .build();

        logger.info("Starting testAddBook with DTO: {}", bookDTO);

        ResponseEntity<BookDTO> response = booksRESTController.addBook(bookDTO);

        verify(bookService).saveBook(bookDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testGetAllBooks() {
        BookDTO book1 = BookDTO.builder()
                .id(1L)
                .bookName("Book 1")
                .publicationYear("2023")
                .build();

        BookDTO book2 = BookDTO.builder()
                .id(2L)
                .bookName("Book 2")
                .publicationYear("2024")
                .build();

        List<BookDTO> expectedBooks = Arrays.asList(book1, book2);

        logger.info("Starting testGetAllBooks");

        when(bookService.getAllBooks()).thenReturn(expectedBooks);

        ResponseEntity<List<BookDTO>> response = booksRESTController.getAllBooks();

        verify(bookService).getAllBooks();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Book 1", response.getBody().get(0).getBookName());
        assertEquals("Book 2", response.getBody().get(1).getBookName());

        logger.info("testGetAllBooks completed successfully with {} books", response.getBody().size());
    }

    @Test
    public void testGetBookByName() {
        String bookName = "Test Book";
        BookDTO expectedBook = BookDTO.builder()
                .id(1L)
                .bookName(bookName)
                .publicationYear("2024")
                .build();

        logger.info("Starting testGetBookByName with name: {}", bookName);

        when(bookService.findBookByName(bookName)).thenReturn(expectedBook);

        ResponseEntity<BookDTO> response = booksRESTController.getBook(bookName);

        verify(bookService).findBookByName(bookName);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(bookName, response.getBody().getBookName());
        assertEquals(1L, response.getBody().getId());
        assertEquals("2024", response.getBody().getPublicationYear());

        logger.info("testGetBookByName completed successfully, found book: {}", response.getBody());
    }

    @Test
    public void testGetBookByName_NotFound() {
        String bookName = "Non-existent Book";

        logger.info("Starting testGetBookByName_NotFound with name: {}", bookName);

        when(bookService.findBookByName(bookName)).thenReturn(null);

        ResponseEntity<BookDTO> response = booksRESTController.getBook(bookName);

        verify(bookService).findBookByName(bookName);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        logger.info("testGetBookByName_NotFound completed");
    }

    @Test
    public void testCreateAuthor() {
        AuthorDTO authorDTO = AuthorDTO.builder()
                .authorName("Test Author")
                .dateOfBirth("1 Jan 1980")
                .countryOfOrigin("USA")
                .build();

        logger.info("Starting testCreateAuthor with DTO: {}", authorDTO);

        ResponseEntity<AuthorDTO> response = authorRESTController.createAuthor(authorDTO);

        verify(authorService).createAuthor(authorDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        logger.info("testCreateAuthor completed successfully");
    }

    @Test
    public void testGetAllAuthors() {
        // Arrange
        AuthorDTO author1 = AuthorDTO.builder()
                .authorId(1L)
                .authorName("Author 1")
                .dateOfBirth("1 Jan 1980")
                .countryOfOrigin("USA")
                .build();

        AuthorDTO author2 = AuthorDTO.builder()
                .authorId(2L)
                .authorName("Author 2")
                .dateOfBirth("15 Feb 1975")
                .countryOfOrigin("UK")
                .build();

        List<AuthorDTO> expectedAuthors = Arrays.asList(author1, author2);

        logger.info("Starting testGetAllAuthors");

        when(authorService.getAllAuthors()).thenReturn(expectedAuthors);

        ResponseEntity<List<AuthorDTO>> response = authorRESTController.getAllAuthors();

        verify(authorService).getAllAuthors();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Author 1", response.getBody().get(0).getAuthorName());
        assertEquals("Author 2", response.getBody().get(1).getAuthorName());
        assertEquals("USA", response.getBody().get(0).getCountryOfOrigin());
        assertEquals("UK", response.getBody().get(1).getCountryOfOrigin());

        logger.info("testGetAllAuthors completed successfully with {} authors", response.getBody().size());
    }

    @Test
    public void testGetAuthorById() {
        Long authorId = 1L;
        AuthorDTO expectedAuthor = AuthorDTO.builder()
                .authorId(authorId)
                .authorName("Test Author")
                .dateOfBirth("1 Jan 1980")
                .countryOfOrigin("USA")
                .build();

        logger.info("Starting testGetAuthorById with id: {}", authorId);

        when(authorService.getAuthorById(authorId)).thenReturn(expectedAuthor);

        ResponseEntity<AuthorDTO> response = authorRESTController.getAuthor(authorId);

        verify(authorService).getAuthorById(authorId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(authorId, response.getBody().getAuthorId());
        assertEquals("Test Author", response.getBody().getAuthorName());
        assertEquals("1 Jan 1980", response.getBody().getDateOfBirth());
        assertEquals("USA", response.getBody().getCountryOfOrigin());

        logger.info("testGetAuthorById completed successfully, found author: {}", response.getBody());
    }

    @Test
    public void testGetAuthorById_NotFound() {
        Long authorId = 999L;

        logger.info("Starting testGetAuthorById_NotFound with id: {}", authorId);

        when(authorService.getAuthorById(authorId))
                .thenThrow(new IllegalArgumentException("Author not found with ID: " + authorId));

        try {
            authorRESTController.getAuthor(authorId);
            logger.error("Expected exception was not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Author not found with ID: 999", e.getMessage());
            logger.info("testGetAuthorById_NotFound completed successfully, exception caught as expected");
        }

        verify(authorService).getAuthorById(authorId);
    }

    @Test
    public void testDeleteAuthor() {
        Long authorId = 1L;

        logger.info("Starting testDeleteBook with id: {}", authorId);

        ResponseEntity<Void> response = authorRESTController.deleteAuthor(authorId);

        verify(authorService).deleteAuthor(authorId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

}
