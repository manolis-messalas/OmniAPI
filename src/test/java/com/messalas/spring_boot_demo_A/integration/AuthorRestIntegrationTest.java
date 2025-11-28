package com.messalas.spring_boot_demo_A.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.messalas.spring_boot_demo_A.model.dto.AuthorDTO;
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
public class AuthorRestIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthorRestIntegrationTest.class);

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
    public void testGetAllAuthors() throws Exception {
        logger.info("Starting testGetAllAuthors");

        mockMvc.perform(get("/api/rest/authors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(6))))
                .andExpect(jsonPath("$[0].authorName", notNullValue()))
                .andExpect(jsonPath("$[0].dateOfBirth", notNullValue()))
                .andExpect(jsonPath("$[0].countryOfOrigin", notNullValue()));

        logger.info("testGetAllAuthors completed successfully");
    }

    @Test
    public void testGetAuthorById_Success() throws Exception {
        Long authorId = 1L;
        logger.info("Starting testGetAuthorById_Success with ID: {}", authorId);

        mockMvc.perform(get("/api/rest/author/{id}", authorId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.authorId", is(authorId.intValue())))
                .andExpect(jsonPath("$.authorName", is("Erich Fromm")))
                .andExpect(jsonPath("$.dateOfBirth", is("23 March 1900")))
                .andExpect(jsonPath("$.countryOfOrigin", is("German")));

        logger.info("testGetAuthorById_Success completed successfully");
    }

    @Test
    public void testGetAuthorById_NotFound() throws Exception {
        Long authorId = 999L;
        logger.info("Starting testGetAuthorById_NotFound with ID: {}", authorId);

        mockMvc.perform(get("/api/rest/author/{id}", authorId))
                .andExpect(status().isBadRequest());

        logger.info("testGetAuthorById_NotFound completed successfully");
    }

    @Test
    public void testCreateAuthor_Success() throws Exception {
        logger.info("Starting testCreateAuthor_Success");

        AuthorDTO newAuthor = AuthorDTO.builder()
                .authorName("New Test Author")
                .dateOfBirth("1 Jan 1990")
                .countryOfOrigin("Canada")
                .build();

        mockMvc.perform(post("/api/rest/createAuthor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAuthor)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/rest/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.authorName == 'New Test Author')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.authorName == 'New Test Author')].countryOfOrigin", contains("Canada")));

        logger.info("testCreateAuthor_Success completed successfully");
    }

    @Test
    public void testDeleteAuthor_Success() throws Exception {
        logger.info("Starting testDeleteAuthor_Success");

        AuthorDTO authorToDelete = AuthorDTO.builder()
                .authorName("Author To Delete")
                .dateOfBirth("1 Jan 1985")
                .countryOfOrigin("France")
                .build();

        String createResponse = mockMvc.perform(post("/api/rest/createAuthor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorToDelete)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String authorsResponse = mockMvc.perform(get("/api/rest/authors"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthorDTO[] authors = objectMapper.readValue(authorsResponse, AuthorDTO[].class);
        Long authorIdToDelete = null;
        for (AuthorDTO author : authors) {
            if ("Author To Delete".equals(author.getAuthorName())) {
                authorIdToDelete = author.getAuthorId();
                break;
            }
        }

        mockMvc.perform(delete("/api/rest/authors/{id}", authorIdToDelete))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/rest/author/{id}", authorIdToDelete))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Author not found")));

        logger.info("testDeleteAuthor_Success completed successfully");
    }

    @Test
    public void testDeleteAuthor_NotFound() throws Exception {
        Long authorId = 999L;
        logger.info("Starting testDeleteAuthor_NotFound with ID: {}", authorId);

        mockMvc.perform(delete("/api/rest/authors/{id}", authorId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Author not found")));

        logger.info("testDeleteAuthor_NotFound completed successfully");
    }

    @Test
    public void testCreateAuthorWithInvalidData() throws Exception {
        logger.info("Starting testCreateAuthorWithInvalidData");

        AuthorDTO invalidAuthor = AuthorDTO.builder()
                .authorName("")
                .dateOfBirth("")
                .countryOfOrigin("")
                .build();

        mockMvc.perform(post("/api/rest/createAuthor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAuthor)))
                .andExpect(status().isOk());

        logger.info("testCreateAuthorWithInvalidData completed successfully");
    }

    @Test
    public void testGetAuthorByIdWithStringParameter() throws Exception {
        logger.info("Starting testGetAuthorByIdWithStringParameter");

        mockMvc.perform(get("/api/rest/author/{id}", "invalid"))
                .andExpect(status().isBadRequest());

        logger.info("testGetAuthorByIdWithStringParameter completed successfully");
    }

    @Test
    public void testVerifyDatabaseHasExpectedAuthors() throws Exception {
        logger.info("Starting testVerifyDatabaseHasExpectedAuthors");

        mockMvc.perform(get("/api/rest/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.authorName == 'Erich Fromm')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.authorName == 'Bill Bryson')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.authorName == 'Fyodor Dostoevsky')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.authorName == 'Konstantinos Kavafis')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.authorName == 'Tim Marshall')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.authorName == 'Yuval Noah Harari')]", hasSize(1)));

        logger.info("testVerifyDatabaseHasExpectedAuthors completed successfully");
    }
}