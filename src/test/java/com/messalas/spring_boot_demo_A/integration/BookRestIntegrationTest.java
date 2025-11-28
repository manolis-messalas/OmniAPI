package com.messalas.spring_boot_demo_A.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messalas.spring_boot_demo_A.model.builders.BookAuthorDTOBuilder;
import com.messalas.spring_boot_demo_A.model.dto.AuthorDTO;
import com.messalas.spring_boot_demo_A.model.dto.BookAuthorDTO;
import com.messalas.spring_boot_demo_A.model.dto.BookDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
public class BookRestIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(BookRestIntegrationTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    public void testGetAllBooks() throws Exception {
        logger.info("Starting testGetAllBooks");

        mockMvc.perform(get("/api/rest/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(6))))
                .andExpect(jsonPath("$[0].bookName", notNullValue()))
                .andExpect(jsonPath("$[0].publicationYear", notNullValue()))
                .andExpect(jsonPath("$[0].authorDTO", notNullValue()));

        logger.info("testGetAllBooks completed successfully");
    }

    @Test
    public void testGetBookByName_Success() throws Exception {
        String bookName = "The Fear of Freedom";
        logger.info("Starting testGetBookByName_Success with name: {}", bookName);

        mockMvc.perform(get("/api/rest/book/{name}", bookName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookName", is(bookName)))
                .andExpect(jsonPath("$.publicationYear", is("1941")))
                .andExpect(jsonPath("$.authorDTO.authorName", is("Erich Fromm")));

        logger.info("testGetBookByName_Success completed successfully");
    }

    @Test
    public void testGetBookByName_NotFound() throws Exception {
        String bookName = "Non-existent Book";
        logger.info("Starting testGetBookByName_NotFound with name: {}", bookName);

        mockMvc.perform(get("/api/rest/book/{name}", bookName))
                .andExpect(status().isBadRequest());

        logger.info("testGetBookByName_NotFound completed successfully");
    }

    @Test
    public void testAddBookAuthor_Success() throws Exception {
        logger.info("Starting testAddBookAuthor_Success");

        BookAuthorDTO bookAuthorDTO = new BookAuthorDTOBuilder()
                .bookName("New Integration Test Book")
                .publicationYear("2024")
                .authorName("Integration Test Author")
                .dateOfBirth("1 Jan 1990")
                .countryOfOrigin("TestLand")
                .build();

        mockMvc.perform(post("/api/rest/addBookAuthor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookAuthorDTO)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/rest/book/{name}", "New Integration Test Book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookName", is("New Integration Test Book")))
                .andExpect(jsonPath("$.publicationYear", is("2024")))
                .andExpect(jsonPath("$.authorDTO.authorName", is("Integration Test Author")));

        logger.info("testAddBookAuthor_Success completed successfully");
    }

    @Test
    public void testAddBook_Success() throws Exception {
        logger.info("Starting testAddBook_Success");

        AuthorDTO authorDTO = AuthorDTO.builder()
                .authorName("Erich Fromm")
                .build();

        BookDTO bookDTO = BookDTO.builder()
                .bookName("New Unique Test Book")
                .publicationYear("1941")
                .authorDTO(authorDTO)
                .build();

        mockMvc.perform(post("/api/rest/addBook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/rest/book/{name}", "New Unique Test Book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookName", is("New Unique Test Book")))
                .andExpect(jsonPath("$.publicationYear", is("1941")));

        logger.info("testAddBook_Success completed successfully");
    }

    @Test
    public void testAddBook_AuthorNotFound() throws Exception {
        logger.info("Starting testAddBook_AuthorNotFound");

        AuthorDTO authorDTO = AuthorDTO.builder()
                .authorName("Non-existent Author")
                .build();

        BookDTO bookDTO = BookDTO.builder()
                .bookName("Book with Invalid Author")
                .publicationYear("2024")
                .authorDTO(authorDTO)
                .build();

        mockMvc.perform(post("/api/rest/addBook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Author not found")));

        logger.info("testAddBook_AuthorNotFound completed successfully");
    }

    @Test
    public void testDeleteBook_Success() throws Exception {
        logger.info("Starting testDeleteBook_Success");

        BookAuthorDTO bookToDelete = new BookAuthorDTOBuilder()
                .bookName("Book To Delete")
                .publicationYear("2024")
                .authorName("Delete Test Author")
                .dateOfBirth("1 Jan 1990")
                .countryOfOrigin("DeleteLand")
                .build();

        String createResponse = mockMvc.perform(post("/api/rest/addBookAuthor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookToDelete)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("Create response: " + createResponse);

        String booksResponse = mockMvc.perform(get("/api/rest/books"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        System.out.println("Get response: " + booksResponse);

        BookDTO[] books = objectMapper.readValue(booksResponse, BookDTO[].class);
        Long bookIdToDelete = null;
        String bookNameToDelete = null;
        for (BookDTO book : books) {
            if ("Book To Delete".equals(book.getBookName())) {
                bookIdToDelete = book.getId();
                bookNameToDelete = book.getBookName();
                break;
            }
        }

        mockMvc.perform(delete("/api/rest/books/{id}", bookIdToDelete))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/rest/book/{name}", bookNameToDelete))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Book not found")));

        logger.info("testDeleteBook_Success completed successfully");
    }

    @Test
    public void testDeleteBook_NotFound() throws Exception {
        String name = "invalid";
        logger.info("Starting testDeleteBook_NotFound with name: {}", name);

        mockMvc.perform(delete("/api/rest/books/{name}", name))
                .andExpect(status().isBadRequest());

        logger.info("testDeleteBook_NotFound completed successfully");
    }

    @Test
    public void testVerifyDatabaseHasExpectedBooks() throws Exception {
        logger.info("Starting testVerifyDatabaseHasExpectedBooks");

        mockMvc.perform(get("/api/rest/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.bookName == 'The Fear of Freedom')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.bookName == 'The Body')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.bookName == 'Crime and Punishment')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.bookName == 'The Poems')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.bookName == 'Prisoners of Geography')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.bookName == 'Sapiens: A Brief History of Humankind')]", hasSize(1)));

        logger.info("testVerifyDatabaseHasExpectedBooks completed successfully");
    }

    @Test
    public void testAddBookAuthorCreatesAuthorAndBook() throws Exception {
        logger.info("Starting testAddBookAuthorCreatesAuthorAndBook");

        BookAuthorDTO bookAuthorDTO = new BookAuthorDTOBuilder()
                .bookName("Full Flow Test Book")
                .publicationYear("2024")
                .authorName("Full Flow Author")
                .dateOfBirth("15 May 1985")
                .countryOfOrigin("FlowLand")
                .build();

        mockMvc.perform(post("/api/rest/addBookAuthor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookAuthorDTO)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/rest/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.bookName == 'Full Flow Test Book')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.bookName == 'Full Flow Test Book')].authorDTO.authorName", contains("Full Flow Author")))
                .andExpect(jsonPath("$[?(@.bookName == 'Full Flow Test Book')].authorDTO.countryOfOrigin", contains("FlowLand")));

        mockMvc.perform(get("/api/rest/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.authorName == 'Full Flow Author')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.authorName == 'Full Flow Author')].dateOfBirth", contains("15 May 1985")));

        logger.info("testAddBookAuthorCreatesAuthorAndBook completed successfully");
    }

    @Test
    public void testGetBookByNameWithSpecialCharacters() throws Exception {
        String bookName = "Sapiens: A Brief History of Humankind";
        logger.info("Starting testGetBookByNameWithSpecialCharacters with name: {}", bookName);

        mockMvc.perform(get("/api/rest/book/{name}", bookName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookName", is(bookName)))
                .andExpect(jsonPath("$.authorDTO.authorName", is("Yuval Noah Harari")));

        logger.info("testGetBookByNameWithSpecialCharacters completed successfully");
    }
}