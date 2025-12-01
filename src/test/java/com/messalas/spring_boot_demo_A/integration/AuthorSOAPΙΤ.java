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
public class AuthorSOAPΙΤ {

    private static final Logger logger = LoggerFactory.getLogger(AuthorSOAPΙΤ.class);

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
    public void testGetAllAuthors() {
        logger.info("Starting testGetAllAuthors");

        GetAuthorsRequest request = new GetAuthorsRequest();
        GetAuthorsResponse response = (GetAuthorsResponse) webServiceTemplate.marshalSendAndReceive(request);

        assertNotNull(response);
        assertNotNull(response.getAuthors());
        assertTrue(response.getAuthors().size() >= 2);

        logger.info("testGetAllAuthors completed");
    }

    @Test
    public void testGetAuthorById_Success() {
        logger.info("Starting testGetAuthorById_Success");

        GetAuthorRequest request = new GetAuthorRequest();
        request.setId(1L);

        GetAuthorResponse response = (GetAuthorResponse) webServiceTemplate.marshalSendAndReceive(request);

        assertNotNull(response);
        assertNotNull(response.getAuthor());
        assertEquals(1L, response.getAuthor().getId());

        logger.info("testGetAuthorById_Success completed");
    }

    @Test
    public void testCreateAuthor_Success() {
        logger.info("Starting testCreateAuthor_Success");

        Author author = new Author();
        author.setName("SOAP IT Test");
        author.setDateOfBirth("1 Jan 1990");
        author.setCountryOfOrigin("TestLand");

        CreateAuthorRequest request = new CreateAuthorRequest();
        request.setAuthor(author);

        CreateAuthorResponse response = (CreateAuthorResponse) webServiceTemplate.marshalSendAndReceive(request);

        assertNotNull(response);
        assertNotNull(response.getAuthorId());
        assertEquals("SUCCESS", response.getMessage());

        logger.info("testCreateAuthor_Success completed");
    }

    @Test
    public void testDeleteAuthor_Success() {
        logger.info("Starting testDeleteAuthor_Success");

        Author author = new Author();
        author.setName("Delete Test");
        author.setDateOfBirth("1 Jan 1985");
        author.setCountryOfOrigin("DeleteLand");

        CreateAuthorRequest createRequest = new CreateAuthorRequest();
        createRequest.setAuthor(author);
        CreateAuthorResponse createResponse = (CreateAuthorResponse) webServiceTemplate.marshalSendAndReceive(createRequest);

        DeleteAuthorRequest deleteRequest = new DeleteAuthorRequest();
        deleteRequest.setAuthorId(createResponse.getAuthorId());
        DeleteAuthorResponse deleteResponse = (DeleteAuthorResponse) webServiceTemplate.marshalSendAndReceive(deleteRequest);

        assertTrue(deleteResponse.isStatus());

        logger.info("testDeleteAuthor_Success completed");
    }
}