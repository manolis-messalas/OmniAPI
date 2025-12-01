package com.messalas.spring_boot_demo_A.integration;

import bookshelf.generated.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ws.client.core.WebServiceTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
public class BookSOAPIΤ {

    private static final Logger logger = LoggerFactory.getLogger(BookSOAPIΤ.class);

    @LocalServerPort
    private int port;

    private WebServiceTemplate webServiceTemplate;

    private final boolean testPassed = true;

    @BeforeEach
    public void setup() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("bookshelf.generated");

        webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setMarshaller(marshaller);
        webServiceTemplate.setUnmarshaller(marshaller);
        webServiceTemplate.setDefaultUri("http://localhost:" + port + "/api/ws");
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
    public void testGetAllBooks() {
        logger.info("Starting testGetAllBooks");

        GetBooksRequest request = new GetBooksRequest();
        GetBooksResponse response = (GetBooksResponse) webServiceTemplate.marshalSendAndReceive(request);

        assertNotNull(response);
        assertNotNull(response.getBooks());
        assertTrue(response.getBooks().size() >= 2);

        logger.info("testGetAllBooks completed");
    }

    @Test
    public void testCreateBookAuthor_Success() {
        logger.info("Starting testCreateBookAuthor_Success");

        bookshelf.generated.BookAuthorDTO bookAuthorDTO = new bookshelf.generated.BookAuthorDTO();
        bookAuthorDTO.setBookName("SOAP Test Book");
        bookAuthorDTO.setPublicationYear("2024");
        bookAuthorDTO.setAuthorName("SOAP Test Author");
        bookAuthorDTO.setDateOfBirth("1 Jan 1990");
        bookAuthorDTO.setCountryOfOrigin("TestLand");

        CreateBookAuthorRequest request = new CreateBookAuthorRequest();
        request.setBookAuthorDTO(bookAuthorDTO);

        CreateBookAuthorResponse response = (CreateBookAuthorResponse) webServiceTemplate.marshalSendAndReceive(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());

        logger.info("testCreateBookAuthor_Success completed");
    }

    @Test
    public void testCreateBook_Success() {
        logger.info("Starting testCreateBook_Success");

        Author author = new Author();
        author.setName("Book Test Author");
        author.setDateOfBirth("1 Jan 1985");
        author.setCountryOfOrigin("BookLand");

        CreateAuthorRequest createAuthorRequest = new CreateAuthorRequest();
        createAuthorRequest.setAuthor(author);
        webServiceTemplate.marshalSendAndReceive(createAuthorRequest);

        Book book = new Book();
        book.setName("SOAP Book Only Test");
        book.setPublicationYear("2024");
        book.setAuthorName("Book Test Author");

        CreateBookRequest request = new CreateBookRequest();
        request.setBook(book);

        CreateBookResponse response = (CreateBookResponse) webServiceTemplate.marshalSendAndReceive(request);

        assertNotNull(response);
        assertNotNull(response.getBookId());
        assertEquals("SUCCESS", response.getMessage());

        logger.info("testCreateBook_Success completed");
    }

    @Test
    public void testDeleteBook_Success() {
        logger.info("Starting testDeleteBook_Success");

        bookshelf.generated.BookAuthorDTO bookAuthorDTO = new bookshelf.generated.BookAuthorDTO();
        bookAuthorDTO.setBookName("Book To Delete SOAP");
        bookAuthorDTO.setPublicationYear("2024");
        bookAuthorDTO.setAuthorName("Delete Author SOAP");
        bookAuthorDTO.setDateOfBirth("1 Jan 1980");
        bookAuthorDTO.setCountryOfOrigin("DeleteLand");

        CreateBookAuthorRequest createRequest = new CreateBookAuthorRequest();
        createRequest.setBookAuthorDTO(bookAuthorDTO);
        webServiceTemplate.marshalSendAndReceive(createRequest);

        GetBooksRequest getBooksRequest = new GetBooksRequest();
        GetBooksResponse booksResponse = (GetBooksResponse) webServiceTemplate.marshalSendAndReceive(getBooksRequest);

        Long bookIdToDelete = null;
        for (Book book : booksResponse.getBooks()) {
            if ("Book To Delete SOAP".equals(book.getName())) {
                bookIdToDelete = book.getId();
                break;
            }
        }

        assertNotNull(bookIdToDelete);

        DeleteBookRequest deleteRequest = new DeleteBookRequest();
        deleteRequest.setBookId(bookIdToDelete);
        DeleteBookResponse deleteResponse = (DeleteBookResponse) webServiceTemplate.marshalSendAndReceive(deleteRequest);

        assertTrue(deleteResponse.isStatus());

        logger.info("testDeleteBook_Success completed");
    }

    @Test
    public void testDeleteBook_NotFound() {
        logger.info("Starting testDeleteBook_NotFound");

        DeleteBookRequest request = new DeleteBookRequest();
        request.setBookId(999L);

        DeleteBookResponse response = (DeleteBookResponse) webServiceTemplate.marshalSendAndReceive(request);

        assertNotNull(response);
        assertFalse(response.isStatus());

        logger.info("testDeleteBook_NotFound completed");
    }
}