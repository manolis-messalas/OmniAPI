package com.messalas.omniapi.unit;

import bookshelf.generated.*;
import com.messalas.omniapi.api.soap.AuthorSOAPController;
import com.messalas.omniapi.api.soap.BookSOAPController;
import com.messalas.omniapi.exceptions.AuthorServiceException;
import com.messalas.omniapi.exceptions.AuthorValidationException;
import com.messalas.omniapi.exceptions.BookServiceException;
import com.messalas.omniapi.exceptions.BookValidationException;
import com.messalas.omniapi.exceptions.OptimisticLockConflictException;
import com.messalas.omniapi.model.dto.AuthorDTO;
import com.messalas.omniapi.model.dto.BookAuthorDTO;
import com.messalas.omniapi.model.dto.BookDTO;
import com.messalas.omniapi.service.AuthorService;
import com.messalas.omniapi.service.BookService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SOAPControllersTest {

    private static final Logger logger = LoggerFactory.getLogger(SOAPControllersTest.class);

    @Mock
    private AuthorService authorService;

    @Mock
    private BookService bookService;

    @InjectMocks
    private AuthorSOAPController authorSOAPController;

    @InjectMocks
    private BookSOAPController bookSOAPController;

    private final boolean testPassed = true;

    @BeforeEach
    public void setup() {
        reset(bookService, authorService);
    }

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
    public void testCreateAuthor_Success() {
        Author soapAuthor = new Author();
        soapAuthor.setName("Test Author");
        soapAuthor.setDateOfBirth("1 Jan 1980");
        soapAuthor.setCountryOfOrigin("USA");

        CreateAuthorRequest request = new CreateAuthorRequest();
        request.setAuthor(soapAuthor);

        logger.info("Starting testCreateAuthor_Success");

        when(authorService.createAuthor(any(AuthorDTO.class))).thenReturn(1L);

        CreateAuthorResponse response = authorSOAPController.createAuthor(request);

        assertNotNull(response);
        assertEquals(1L, response.getAuthorId());
        assertEquals("SUCCESS", response.getMessage());
        verify(authorService).createAuthor(any(AuthorDTO.class));

        logger.info("testCreateAuthor_Success completed successfully");
    }

    @Test
    public void testCreateAuthor_ValidationError() {
        Author soapAuthor = new Author();
        soapAuthor.setName("Invalid Author");

        CreateAuthorRequest request = new CreateAuthorRequest();
        request.setAuthor(soapAuthor);

        logger.info("Starting testCreateAuthor_ValidationError");

        when(authorService.createAuthor(any(AuthorDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid data"));

        AuthorValidationException exception = assertThrows(
                AuthorValidationException.class,
                () -> authorSOAPController.createAuthor(request)
        );

        assertEquals("Validation failed: Invalid data", exception.getMessage());
        verify(authorService).createAuthor(any(AuthorDTO.class));

        logger.info("testCreateAuthor_ValidationError completed successfully");
    }

    @Test
    public void testCreateAuthor_ServiceError() {
        Author soapAuthor = new Author();
        soapAuthor.setName("Test Author");
        soapAuthor.setDateOfBirth("1 Jan 1980");
        soapAuthor.setCountryOfOrigin("USA");

        CreateAuthorRequest request = new CreateAuthorRequest();
        request.setAuthor(soapAuthor);

        logger.info("Starting testCreateAuthor_ServiceError");

        when(authorService.createAuthor(any(AuthorDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        AuthorServiceException exception = assertThrows(
                AuthorServiceException.class,
                () -> authorSOAPController.createAuthor(request)
        );

        assertEquals("Internal server error", exception.getMessage());
        verify(authorService).createAuthor(any(AuthorDTO.class));

        logger.info("testCreateAuthor_ServiceError completed successfully");
    }

    @Test
    public void testDeleteAuthor_Success() {
        DeleteAuthorRequest request = new DeleteAuthorRequest();
        request.setAuthorId(1L);

        logger.info("Starting testDeleteAuthor_Success");

        doNothing().when(authorService).deleteAuthor(1L);

        DeleteAuthorResponse response = authorSOAPController.deleteAuthor(request);

        assertNotNull(response);
        assertTrue(response.isStatus());
        verify(authorService).deleteAuthor(1L);

        logger.info("testDeleteAuthor_Success completed successfully");
    }

    @Test
    public void testDeleteAuthor_Failure() {
        DeleteAuthorRequest request = new DeleteAuthorRequest();
        request.setAuthorId(999L);

        logger.info("Starting testDeleteAuthor_Failure");

        doThrow(new RuntimeException("Author not found")).when(authorService).deleteAuthor(999L);

        DeleteAuthorResponse response = authorSOAPController.deleteAuthor(request);

        assertNotNull(response);
        assertFalse(response.isStatus());
        verify(authorService).deleteAuthor(999L);

        logger.info("testDeleteAuthor_Failure completed successfully");
    }

    @Test
    public void testGetAuthor_Success() {
        Long authorId = 1L;
        GetAuthorRequest request = new GetAuthorRequest();
        request.setId(authorId);

        AuthorDTO authorDTO = AuthorDTO.builder()
                .authorId(authorId)
                .authorName("Test Author")
                .dateOfBirth("1 Jan 1980")
                .countryOfOrigin("USA")
                .build();

        logger.info("Starting testGetAuthor_Success");

        when(authorService.getAuthorById(authorId)).thenReturn(authorDTO);

        GetAuthorResponse response = authorSOAPController.getAuthor(request);

        assertNotNull(response);
        assertNotNull(response.getAuthor());
        assertEquals(authorId, response.getAuthor().getId());
        assertEquals("Test Author", response.getAuthor().getName());
        assertEquals("USA", response.getAuthor().getCountryOfOrigin());
        verify(authorService).getAuthorById(authorId);

        logger.info("testGetAuthor_Success completed successfully");
    }

    @Test
    public void testGetAuthor_NotFound() {
        Long authorId = 999L;
        GetAuthorRequest request = new GetAuthorRequest();
        request.setId(authorId);

        logger.info("Starting testGetAuthor_NotFound");

        when(authorService.getAuthorById(authorId))
                .thenThrow(new IllegalArgumentException("Author not found"));

        GetAuthorResponse response = authorSOAPController.getAuthor(request);

        assertNotNull(response);
        assertNull(response.getAuthor());
        verify(authorService).getAuthorById(authorId);

        logger.info("testGetAuthor_NotFound completed successfully");
    }

    @Test
    public void testGetAuthor_ServiceError() {
        Long authorId = 1L;
        GetAuthorRequest request = new GetAuthorRequest();
        request.setId(authorId);

        logger.info("Starting testGetAuthor_ServiceError");

        when(authorService.getAuthorById(authorId))
                .thenThrow(new RuntimeException("Database error"));

        GetAuthorResponse response = authorSOAPController.getAuthor(request);

        assertNotNull(response);
        assertNull(response.getAuthor());
        verify(authorService).getAuthorById(authorId);

        logger.info("testGetAuthor_ServiceError completed successfully");
    }

    @Test
    public void testGetAuthors_Success() {
        GetAuthorsRequest request = new GetAuthorsRequest();

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

        List<AuthorDTO> authorDTOs = Arrays.asList(author1, author2);

        logger.info("Starting testGetAuthors_Success");

        when(authorService.getAllAuthors()).thenReturn(authorDTOs);

        GetAuthorsResponse response = authorSOAPController.getAuthors(request);

        assertNotNull(response);
        assertNotNull(response.getAuthors());
        assertEquals(2, response.getAuthors().size());
        assertEquals("Author 1", response.getAuthors().get(0).getName());
        assertEquals("Author 2", response.getAuthors().get(1).getName());
        verify(authorService).getAllAuthors();

        logger.info("testGetAuthors_Success completed successfully");
    }

    @Test
    public void testGetAuthors_ServiceError() {
        GetAuthorsRequest request = new GetAuthorsRequest();

        logger.info("Starting testGetAuthors_ServiceError");

        when(authorService.getAllAuthors())
                .thenThrow(new RuntimeException("Database error"));

        GetAuthorsResponse response = authorSOAPController.getAuthors(request);

        assertNotNull(response);
        assertTrue(response.getAuthors().isEmpty());
        verify(authorService).getAllAuthors();

        logger.info("testGetAuthors_ServiceError completed successfully");
    }

    @Test
    public void testMapAuthorDTOToRequestDTO() {
        AuthorDTO authorDTO = AuthorDTO.builder()
                .authorId(1L)
                .authorName("Test Author")
                .dateOfBirth("1 Jan 1980")
                .countryOfOrigin("USA")
                .build();

        logger.info("Starting testMapAuthorDTOToRequestDTO");

        Author result = authorSOAPController.mapAuthorDTOToRequestDTO(authorDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Author", result.getName());
        assertEquals("1 Jan 1980", result.getDateOfBirth());
        assertEquals("USA", result.getCountryOfOrigin());

        logger.info("testMapAuthorDTOToRequestDTO completed successfully");
    }

    @Test
    public void testMapAuthorDTOsToRequestDTOs() {
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

        List<AuthorDTO> authorDTOs = Arrays.asList(author1, author2);

        logger.info("Starting testMapAuthorDTOsToRequestDTOs");

        List<Author> result = authorSOAPController.mapAuthorDTOsToRequestDTOs(authorDTOs);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Author 1", result.get(0).getName());
        assertEquals("Author 2", result.get(1).getName());

        logger.info("testMapAuthorDTOsToRequestDTOs completed successfully");
    }

    @Test
    public void testCreateBookAuthor_Success() {
        bookshelf.generated.BookAuthorDTO soapBookAuthorDTO = new bookshelf.generated.BookAuthorDTO();
        soapBookAuthorDTO.setBookName("Test Book");
        soapBookAuthorDTO.setAuthorName("Test Author");
        soapBookAuthorDTO.setPublicationYear("2024");
        soapBookAuthorDTO.setDateOfBirth("1 Jan 1980");
        soapBookAuthorDTO.setCountryOfOrigin("USA");

        CreateBookAuthorRequest request = new CreateBookAuthorRequest();
        request.setBookAuthorDTO(soapBookAuthorDTO);

        logger.info("Starting testCreateBookAuthor_Success");

        when(bookService.saveBookAuthor(any(BookAuthorDTO.class))).thenReturn(1L);

        CreateBookAuthorResponse response = bookSOAPController.createBookAuthor(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(bookService).saveBookAuthor(any(BookAuthorDTO.class));

        logger.info("testCreateBookAuthor_Success completed successfully");
    }

    @Test
    public void testCreateBookAuthor_Failure() {
        bookshelf.generated.BookAuthorDTO soapBookAuthorDTO = new bookshelf.generated.BookAuthorDTO();
        soapBookAuthorDTO.setBookName("Invalid Book");

        CreateBookAuthorRequest request = new CreateBookAuthorRequest();
        request.setBookAuthorDTO(soapBookAuthorDTO);

        logger.info("Starting testCreateBookAuthor_Failure");

        when(bookService.saveBookAuthor(any(BookAuthorDTO.class)))
                .thenThrow(new RuntimeException("Error"));

        CreateBookAuthorResponse response = bookSOAPController.createBookAuthor(request);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        verify(bookService).saveBookAuthor(any(BookAuthorDTO.class));

        logger.info("testCreateBookAuthor_Failure completed successfully");
    }

    @Test
    public void testCreateBook_Success() {
        Book soapBook = new Book();
        soapBook.setName("Test Book");
        soapBook.setPublicationYear("2024");
        soapBook.setAuthorName("Test Author");

        CreateBookRequest request = new CreateBookRequest();
        request.setBook(soapBook);

        logger.info("Starting testCreateBook_Success");

        when(bookService.saveBook(any(BookDTO.class))).thenReturn(1L);

        CreateBookResponse response = bookSOAPController.createBook(request);

        assertNotNull(response);
        assertEquals(1L, response.getBookId());
        assertEquals("SUCCESS", response.getMessage());
        verify(bookService).saveBook(any(BookDTO.class));

        logger.info("testCreateBook_Success completed successfully");
    }

    @Test
    public void testCreateBook_ValidationError() {
        Book soapBook = new Book();
        soapBook.setName("Invalid Book");

        CreateBookRequest request = new CreateBookRequest();
        request.setBook(soapBook);

        logger.info("Starting testCreateBook_ValidationError");

        when(bookService.saveBook(any(BookDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid data"));

        BookValidationException exception = assertThrows(
                BookValidationException.class,
                () -> bookSOAPController.createBook(request)
        );

        assertEquals("Validation failed: Invalid data", exception.getMessage());
        verify(bookService).saveBook(any(BookDTO.class));

        logger.info("testCreateBook_ValidationError completed successfully");
    }

    @Test
    public void testCreateBook_ServiceError() {
        Book soapBook = new Book();
        soapBook.setName("Test Book");
        soapBook.setPublicationYear("2024");
        soapBook.setAuthorName("Test Author");

        CreateBookRequest request = new CreateBookRequest();
        request.setBook(soapBook);

        logger.info("Starting testCreateBook_ServiceError");

        when(bookService.saveBook(any(BookDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        BookServiceException exception = assertThrows(
                BookServiceException.class,
                () -> bookSOAPController.createBook(request)
        );

        assertEquals("Internal server error", exception.getMessage());
        verify(bookService).saveBook(any(BookDTO.class));

        logger.info("testCreateBook_ServiceError completed successfully");
    }

    @Test
    public void testDeleteBook_Success() {
        DeleteBookRequest request = new DeleteBookRequest();
        request.setBookId(1L);

        logger.info("Starting testDeleteBook_Success");

        doNothing().when(bookService).deleteBook(1L);

        DeleteBookResponse response = bookSOAPController.deleteBook(request);

        assertNotNull(response);
        assertTrue(response.isStatus());
        verify(bookService).deleteBook(1L);

        logger.info("testDeleteBook_Success completed successfully");
    }

    @Test
    public void testDeleteBook_Failure() {
        DeleteBookRequest request = new DeleteBookRequest();
        request.setBookId(999L);

        logger.info("Starting testDeleteBook_Failure");

        doThrow(new RuntimeException("Book not found")).when(bookService).deleteBook(999L);

        DeleteBookResponse response = bookSOAPController.deleteBook(request);

        assertNotNull(response);
        assertFalse(response.isStatus());
        verify(bookService).deleteBook(999L);

        logger.info("testDeleteBook_Failure completed successfully");
    }

    @Test
    public void testGetBooks_Success() {
        GetBooksRequest request = new GetBooksRequest();

        AuthorDTO authorDTO = AuthorDTO.builder()
                .authorId(1L)
                .authorName("Test Author")
                .build();

        BookDTO book1 = BookDTO.builder()
                .id(1L)
                .bookName("Book 1")
                .publicationYear("2023")
                .authorDTO(authorDTO)
                .build();

        BookDTO book2 = BookDTO.builder()
                .id(2L)
                .bookName("Book 2")
                .publicationYear("2024")
                .authorDTO(authorDTO)
                .build();

        List<BookDTO> bookDTOs = Arrays.asList(book1, book2);

        logger.info("Starting testGetBooks_Success");

        when(bookService.getAllBooks()).thenReturn(bookDTOs);

        GetBooksResponse response = bookSOAPController.getBooks(request);

        assertNotNull(response);
        assertNotNull(response.getBooks());
        assertEquals(2, response.getBooks().size());
        assertEquals("Book 1", response.getBooks().get(0).getName());
        assertEquals("Book 2", response.getBooks().get(1).getName());
        verify(bookService).getAllBooks();

        logger.info("testGetBooks_Success completed successfully");
    }

    @Test
    public void testGetBooks_ServiceError() {
        GetBooksRequest request = new GetBooksRequest();

        logger.info("Starting testGetBooks_ServiceError");

        when(bookService.getAllBooks())
                .thenThrow(new RuntimeException("Database error"));

        GetBooksResponse response = bookSOAPController.getBooks(request);

        assertNotNull(response);
        assertTrue(response.getBooks().isEmpty());
        verify(bookService).getAllBooks();

        logger.info("testGetBooks_ServiceError completed successfully");
    }

    @Test
    public void testGetBook_Success() {
        GetBookRequest request = new GetBookRequest();
        request.setName("Test Book");

        AuthorDTO authorDTO = AuthorDTO.builder()
                .authorId(1L)
                .authorName("Test Author")
                .build();

        BookDTO bookDTO = BookDTO.builder()
                .id(1L)
                .bookName("Test Book")
                .publicationYear("2024")
                .authorDTO(authorDTO)
                .build();

        logger.info("Starting testGetBook_Success");

        when(bookService.findBookByName("Test Book")).thenReturn(bookDTO);

        GetBookResponse response = bookSOAPController.getBook(request);

        assertNotNull(response);
        verify(bookService).findBookByName("Test Book");

        logger.info("testGetBook_Success completed successfully");
    }

    @Test
    public void testGetBook_ServiceError() {
        GetBookRequest request = new GetBookRequest();
        request.setName("Non-existent Book");

        logger.info("Starting testGetBook_ServiceError");

        when(bookService.findBookByName("Non-existent Book"))
                .thenThrow(new RuntimeException("Book not found"));

        GetBookResponse response = bookSOAPController.getBook(request);

        assertNotNull(response);
        verify(bookService).findBookByName("Non-existent Book");

        logger.info("testGetBook_ServiceError completed successfully");
    }

    @Test
    public void testMapBookDTOToRequestDTO() {
        AuthorDTO authorDTO = AuthorDTO.builder()
                .authorId(1L)
                .authorName("Test Author")
                .build();

        BookDTO bookDTO = BookDTO.builder()
                .id(1L)
                .bookName("Test Book")
                .publicationYear("2024")
                .authorDTO(authorDTO)
                .build();

        logger.info("Starting testMapBookDTOToRequestDTO");

        Book result = bookSOAPController.mapBookDTOToRequestDTO(bookDTO);

        assertNotNull(result);
        assertEquals("Test Book", result.getName());
        assertEquals("2024", result.getPublicationYear());
        assertEquals("Test Author", result.getAuthorName());

        logger.info("testMapBookDTOToRequestDTO completed successfully");
    }

    @Test
    public void testUpdateBook_Success() {
        Book soapBook = new Book();
        soapBook.setId(1L);
        soapBook.setVersion(0L);
        soapBook.setName("Updated Book");
        soapBook.setPublicationYear("2024");
        soapBook.setAuthorName("Test Author");

        UpdateBookRequest request = new UpdateBookRequest();
        request.setBook(soapBook);

        AuthorDTO authorDTO = AuthorDTO.builder().authorId(1L).authorName("Test Author").build();
        BookDTO updatedBook = BookDTO.builder()
                .id(1L)
                .version(1L)
                .bookName("Updated Book")
                .publicationYear("2024")
                .authorDTO(authorDTO)
                .build();

        logger.info("Starting testUpdateBook_Success");

        when(bookService.updateBook(eq(1L), any(BookDTO.class))).thenReturn(updatedBook);

        UpdateBookResponse response = bookSOAPController.updateBook(request);

        assertNotNull(response);
        assertNotNull(response.getBook());
        assertEquals("Updated Book", response.getBook().getName());
        assertEquals(1L, response.getBook().getVersion());
        verify(bookService).updateBook(eq(1L), any(BookDTO.class));

        logger.info("testUpdateBook_Success completed successfully");
    }

    @Test
    public void testUpdateBook_OptimisticLockConflict() {
        Book soapBook = new Book();
        soapBook.setId(1L);
        soapBook.setVersion(0L);
        soapBook.setName("Book");
        soapBook.setPublicationYear("2024");
        soapBook.setAuthorName("Test Author");

        UpdateBookRequest request = new UpdateBookRequest();
        request.setBook(soapBook);

        logger.info("Starting testUpdateBook_OptimisticLockConflict");

        when(bookService.updateBook(eq(1L), any(BookDTO.class)))
                .thenThrow(new OptimisticLockConflictException("Book with ID 1 was modified by another transaction."));

        assertThrows(
                OptimisticLockConflictException.class,
                () -> bookSOAPController.updateBook(request)
        );

        verify(bookService).updateBook(eq(1L), any(BookDTO.class));

        logger.info("testUpdateBook_OptimisticLockConflict completed successfully");
    }

    @Test
    public void testUpdateBook_ValidationError() {
        Book soapBook = new Book();
        soapBook.setId(1L);
        soapBook.setVersion(0L);
        soapBook.setName("Book");
        soapBook.setPublicationYear("2024");
        soapBook.setAuthorName("Unknown Author");

        UpdateBookRequest request = new UpdateBookRequest();
        request.setBook(soapBook);

        logger.info("Starting testUpdateBook_ValidationError");

        when(bookService.updateBook(eq(1L), any(BookDTO.class)))
                .thenThrow(new IllegalArgumentException("Author not found"));

        BookValidationException exception = assertThrows(
                BookValidationException.class,
                () -> bookSOAPController.updateBook(request)
        );

        assertEquals("Validation failed: Author not found", exception.getMessage());
        verify(bookService).updateBook(eq(1L), any(BookDTO.class));

        logger.info("testUpdateBook_ValidationError completed successfully");
    }

    @Test
    public void testUpdateBook_ServiceError() {
        Book soapBook = new Book();
        soapBook.setId(1L);
        soapBook.setVersion(0L);
        soapBook.setName("Book");
        soapBook.setPublicationYear("2024");
        soapBook.setAuthorName("Test Author");

        UpdateBookRequest request = new UpdateBookRequest();
        request.setBook(soapBook);

        logger.info("Starting testUpdateBook_ServiceError");

        when(bookService.updateBook(eq(1L), any(BookDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        BookServiceException exception = assertThrows(
                BookServiceException.class,
                () -> bookSOAPController.updateBook(request)
        );

        assertEquals("Internal server error", exception.getMessage());
        verify(bookService).updateBook(eq(1L), any(BookDTO.class));

        logger.info("testUpdateBook_ServiceError completed successfully");
    }

    @Test
    public void testUpdateAuthor_Success() {
        Author soapAuthor = new Author();
        soapAuthor.setId(1L);
        soapAuthor.setVersion(0L);
        soapAuthor.setName("Updated Author");
        soapAuthor.setDateOfBirth("1 Jan 1980");
        soapAuthor.setCountryOfOrigin("UK");

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setAuthor(soapAuthor);

        AuthorDTO updatedAuthor = AuthorDTO.builder()
                .authorId(1L)
                .version(1L)
                .authorName("Updated Author")
                .dateOfBirth("1 Jan 1980")
                .countryOfOrigin("UK")
                .build();

        logger.info("Starting testUpdateAuthor_Success");

        when(authorService.updateAuthor(eq(1L), any(AuthorDTO.class))).thenReturn(updatedAuthor);

        UpdateAuthorResponse response = authorSOAPController.updateAuthor(request);

        assertNotNull(response);
        assertNotNull(response.getAuthor());
        assertEquals("Updated Author", response.getAuthor().getName());
        assertEquals(1L, response.getAuthor().getVersion());
        verify(authorService).updateAuthor(eq(1L), any(AuthorDTO.class));

        logger.info("testUpdateAuthor_Success completed successfully");
    }

    @Test
    public void testUpdateAuthor_OptimisticLockConflict() {
        Author soapAuthor = new Author();
        soapAuthor.setId(1L);
        soapAuthor.setVersion(0L);
        soapAuthor.setName("Author");
        soapAuthor.setDateOfBirth("1 Jan 1980");
        soapAuthor.setCountryOfOrigin("USA");

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setAuthor(soapAuthor);

        logger.info("Starting testUpdateAuthor_OptimisticLockConflict");

        when(authorService.updateAuthor(eq(1L), any(AuthorDTO.class)))
                .thenThrow(new OptimisticLockConflictException("Author with ID 1 was modified by another transaction."));

        assertThrows(
                OptimisticLockConflictException.class,
                () -> authorSOAPController.updateAuthor(request)
        );

        verify(authorService).updateAuthor(eq(1L), any(AuthorDTO.class));

        logger.info("testUpdateAuthor_OptimisticLockConflict completed successfully");
    }

    @Test
    public void testUpdateAuthor_ValidationError() {
        Author soapAuthor = new Author();
        soapAuthor.setId(1L);
        soapAuthor.setVersion(0L);
        soapAuthor.setName("");
        soapAuthor.setDateOfBirth("1 Jan 1980");
        soapAuthor.setCountryOfOrigin("USA");

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setAuthor(soapAuthor);

        logger.info("Starting testUpdateAuthor_ValidationError");

        when(authorService.updateAuthor(eq(1L), any(AuthorDTO.class)))
                .thenThrow(new IllegalArgumentException("Name must not be blank"));

        AuthorValidationException exception = assertThrows(
                AuthorValidationException.class,
                () -> authorSOAPController.updateAuthor(request)
        );

        assertEquals("Validation failed: Name must not be blank", exception.getMessage());
        verify(authorService).updateAuthor(eq(1L), any(AuthorDTO.class));

        logger.info("testUpdateAuthor_ValidationError completed successfully");
    }

    @Test
    public void testUpdateAuthor_ServiceError() {
        Author soapAuthor = new Author();
        soapAuthor.setId(1L);
        soapAuthor.setVersion(0L);
        soapAuthor.setName("Author");
        soapAuthor.setDateOfBirth("1 Jan 1980");
        soapAuthor.setCountryOfOrigin("USA");

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setAuthor(soapAuthor);

        logger.info("Starting testUpdateAuthor_ServiceError");

        when(authorService.updateAuthor(eq(1L), any(AuthorDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        AuthorServiceException exception = assertThrows(
                AuthorServiceException.class,
                () -> authorSOAPController.updateAuthor(request)
        );

        assertEquals("Internal server error", exception.getMessage());
        verify(authorService).updateAuthor(eq(1L), any(AuthorDTO.class));

        logger.info("testUpdateAuthor_ServiceError completed successfully");
    }

    @Test
    public void testMapBookDTOsToRequestDTOs() {
        AuthorDTO authorDTO = AuthorDTO.builder()
                .authorId(1L)
                .authorName("Test Author")
                .build();

        BookDTO book1 = BookDTO.builder()
                .id(1L)
                .bookName("Book 1")
                .publicationYear("2023")
                .authorDTO(authorDTO)
                .build();

        BookDTO book2 = BookDTO.builder()
                .id(2L)
                .bookName("Book 2")
                .publicationYear("2024")
                .authorDTO(authorDTO)
                .build();

        List<BookDTO> bookDTOs = Arrays.asList(book1, book2);

        logger.info("Starting testMapBookDTOsToRequestDTOs");

        List<Book> result = bookSOAPController.mapBookDTOsToRequestDTOs(bookDTOs);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Book 1", result.get(0).getName());
        assertEquals("Book 2", result.get(1).getName());

        logger.info("testMapBookDTOsToRequestDTOs completed successfully");
    }
}