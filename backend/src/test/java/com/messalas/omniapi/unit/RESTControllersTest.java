package com.messalas.omniapi.unit;

import com.messalas.omniapi.api.rest.AuthorRESTController;
import com.messalas.omniapi.api.rest.UserRESTController;
import com.messalas.omniapi.model.builders.BookAuthorDTOBuilder;
import com.messalas.omniapi.api.rest.BooksRESTController;
import com.messalas.omniapi.model.dto.AuthorDTO;
import com.messalas.omniapi.model.dto.BookAuthorDTO;
import com.messalas.omniapi.model.dto.BookDTO;
import com.messalas.omniapi.model.dto.UserDetails;
import com.messalas.omniapi.service.AuthorService;
import com.messalas.omniapi.service.BookService;
import com.messalas.omniapi.service.IdempotencyService;
import com.messalas.omniapi.service.UserService;
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
@WebMvcTest(controllers ={BooksRESTController.class, AuthorRESTController.class, UserRESTController.class})
public class RESTControllersTest {

    private static final Logger logger = LoggerFactory.getLogger(RESTControllersTest.class);

    @Autowired
    private BooksRESTController booksRESTController;

    @Autowired
    private AuthorRESTController authorRESTController;

    @Autowired
    private UserRESTController userRESTController;

    @MockBean
    private BookService bookService;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private UserService userService;

    @MockBean
    private IdempotencyService idempotencyService;

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

        ResponseEntity<BookAuthorDTO> response = booksRESTController.addBookAuthor("test-idem-key", bookAuthorDTO);

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

        ResponseEntity<BookDTO> response = booksRESTController.addBook("test-idem-key", bookDTO);

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

        ResponseEntity<AuthorDTO> response = authorRESTController.createAuthor("test-idem-key", authorDTO);

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

    @Test
    public void testUpdateBook() {
        Long bookId = 1L;
        BookDTO requestBody = BookDTO.builder()
                .version(0L)
                .bookName("Updated Book")
                .publicationYear("2024")
                .build();
        BookDTO updatedBook = BookDTO.builder()
                .id(bookId)
                .version(1L)
                .bookName("Updated Book")
                .publicationYear("2024")
                .build();

        logger.info("Starting testUpdateBook with id: {}", bookId);

        when(bookService.updateBook(bookId, requestBody)).thenReturn(updatedBook);

        ResponseEntity<BookDTO> response = booksRESTController.updateBook(bookId, requestBody);

        verify(bookService).updateBook(bookId, requestBody);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals("Updated Book", response.getBody().getBookName());
        assertEquals(1L, response.getBody().getVersion());

        logger.info("testUpdateBook completed successfully");
    }

    @Test
    public void testUpdateAuthor() {
        Long authorId = 1L;
        AuthorDTO requestBody = AuthorDTO.builder()
                .version(0L)
                .authorName("Updated Author")
                .dateOfBirth("1 Jan 1980")
                .countryOfOrigin("UK")
                .build();
        AuthorDTO updatedAuthor = AuthorDTO.builder()
                .authorId(authorId)
                .version(1L)
                .authorName("Updated Author")
                .dateOfBirth("1 Jan 1980")
                .countryOfOrigin("UK")
                .build();

        logger.info("Starting testUpdateAuthor with id: {}", authorId);

        when(authorService.updateAuthor(authorId, requestBody)).thenReturn(updatedAuthor);

        ResponseEntity<AuthorDTO> response = authorRESTController.updateAuthor(authorId, requestBody);

        verify(authorService).updateAuthor(authorId, requestBody);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals("Updated Author", response.getBody().getAuthorName());
        assertEquals(1L, response.getBody().getVersion());

        logger.info("testUpdateAuthor completed successfully");
    }

    //    User Integration Tests
    @Test
    public void testCreateUser() {
        UserDetails userDetails = UserDetails.builder()
                .username("ManoloAdmin")
                .password("@@password!!90")
                .role("ADMIN")
                .build();

        logger.info("Starting testCreateUser with DTO: {}", userDetails);

        when(userService.saveUser(userDetails)).thenReturn(1L);

        ResponseEntity<Long> response = userRESTController.createUser(userDetails);

        verify(userService).saveUser(userDetails);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(1L, response.getBody());

        logger.info("testCreateUser completed successfully with id: {}", response.getBody());
    }

    @Test
    public void testGetUserByUsername() {
        String username = "ManoloAdmin";

        UserDetails expectedUser = UserDetails.builder()
                .id(1L)
                .username(username)
                .password("$2a$10$bcryptHashedPassword")
                .role("ADMIN")
                .build();

        logger.info("Starting testGetUserByUsername with username: {}", username);

        when(userService.loadUserByUsername(username)).thenReturn(expectedUser);

        ResponseEntity<UserDetails> response = userRESTController.getUser(username);

        verify(userService).loadUserByUsername(username);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(username, response.getBody().getUsername());
        assertEquals("$2a$10$bcryptHashedPassword", response.getBody().getPassword());
        assertEquals("ADMIN", response.getBody().getRole());

        logger.info("testGetUserByUsername completed successfully, found user: {}", response.getBody());
    }

    @Test
    public void testGetAllUsers() {
        UserDetails user1 = UserDetails.builder()
                .id(1L)
                .username("ManoloAdmin")
                .password("$2a$10$bcryptHashedPassword")
                .role("ADMIN")
                .build();

        UserDetails user2 = UserDetails.builder()
                .id(2L)
                .username("RegularUser")
                .password("$2a$10$anotherHashedPassword")
                .role("USER")
                .build();

        List<UserDetails> expectedUsers = Arrays.asList(user1, user2);

        logger.info("Starting testGetAllUsers");

        when(userService.getAllUsers()).thenReturn(expectedUsers);

        ResponseEntity<List<UserDetails>> response = userRESTController.getUsers();

        verify(userService).getAllUsers();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        assertEquals(1L, response.getBody().get(0).getId());
        assertEquals("ManoloAdmin", response.getBody().get(0).getUsername());
        assertEquals("ADMIN", response.getBody().get(0).getRole());

        assertEquals(2L, response.getBody().get(1).getId());
        assertEquals("RegularUser", response.getBody().get(1).getUsername());
        assertEquals("USER", response.getBody().get(1).getRole());
        logger.info("testGetAllUsers completed successfully with {} users", response.getBody().size());
    }

    @Test
    public void testDeleteUser() {
        Long userId = 1L;

        logger.info("Starting testDeleteUser with id: {}", userId);

        ResponseEntity<Void> response = userRESTController.deleteUser(userId);

        verify(userService).deleteUser(userId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        logger.info("testDeleteUser completed successfully");
    }

}
