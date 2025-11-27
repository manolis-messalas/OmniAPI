package com.messalas.spring_boot_demo_A.unit;

import com.messalas.spring_boot_demo_A.api.rest.AuthorRESTController;
import com.messalas.spring_boot_demo_A.model.builders.BookAuthorDTOBuilder;
import com.messalas.spring_boot_demo_A.api.rest.BooksRESTController;
import com.messalas.spring_boot_demo_A.model.dto.BookAuthorDTO;
import com.messalas.spring_boot_demo_A.service.AuthorService;
import com.messalas.spring_boot_demo_A.service.BookService;
import org.junit.jupiter.api.AfterEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers ={BooksRESTController.class, AuthorRESTController.class})
public class RESTEndpointsTest {

    private static final Logger logger = LoggerFactory.getLogger(RESTEndpointsTest.class);

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
    public void testCreateBookAuthor(){
        BookAuthorDTO bookAuthorDTO = new BookAuthorDTOBuilder()
                .bookName("manolosbook")
                .authorName("manolo")
                .publicationYear("2024")
                .countryOfOrigin("aktaio")
                .dateOfBirth("14/3/93")
                .build();

        logger.info("Starting testCreateBookAuthor test with DTO: {}", bookAuthorDTO.toString());

        when(bookService.saveBookAuthor(bookAuthorDTO)).thenReturn(2L);
        //Call the controller method
        ResponseEntity<BookAuthorDTO> response = booksRESTController.createBookAuthor(bookAuthorDTO);

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
    public void testDeleteAuthor() {
        Long authorId = 1L;

        logger.info("Starting testDeleteBook with id: {}", authorId);

        ResponseEntity<Void> response = authorRESTController.deleteAuthor(authorId);

        verify(authorService).deleteAuthor(authorId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

}
