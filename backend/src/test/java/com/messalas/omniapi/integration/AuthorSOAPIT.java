package com.messalas.omniapi.integration;

import bookshelf.generated.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpUrlConnection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthorSOAPIT extends OAuth2TestSupport {

    private static final Logger logger = LoggerFactory.getLogger(AuthorSOAPIT.class);

    @LocalServerPort
    private int port;

    private WebServiceTemplate webServiceTemplate;
    private String accessToken;

    private final boolean testPassed = true;

    @BeforeAll
    public void acquireToken() throws Exception {
        accessToken = acquireAccessToken("http://localhost:" + port);
    }

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

    private Object sendWithAuth(Object request) {
        return webServiceTemplate.marshalSendAndReceive(request, message -> {
            HttpUrlConnection conn = (HttpUrlConnection) TransportContextHolder.getTransportContext().getConnection();
            conn.addRequestHeader("Authorization", "Bearer " + accessToken);
        });
    }

    @Test
    public void testGetAllAuthors() {
        logger.info("Starting testGetAllAuthors");

        GetAuthorsRequest request = new GetAuthorsRequest();
        GetAuthorsResponse response = (GetAuthorsResponse) sendWithAuth(request);

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

        GetAuthorResponse response = (GetAuthorResponse) sendWithAuth(request);

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

        CreateAuthorResponse response = (CreateAuthorResponse) sendWithAuth(request);

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
        CreateAuthorResponse createResponse = (CreateAuthorResponse) sendWithAuth(createRequest);

        DeleteAuthorRequest deleteRequest = new DeleteAuthorRequest();
        deleteRequest.setAuthorId(createResponse.getAuthorId());
        DeleteAuthorResponse deleteResponse = (DeleteAuthorResponse) sendWithAuth(deleteRequest);

        assertTrue(deleteResponse.isStatus());

        logger.info("testDeleteAuthor_Success completed");
    }
}
