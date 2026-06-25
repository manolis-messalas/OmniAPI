package com.messalas.omniapi.integration;

import bookshelf.generated.*;
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
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpUrlConnection;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IdempotencySOAPIT extends OAuth2TestSupport {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencySOAPIT.class);

    @LocalServerPort
    private int port;

    private WebServiceTemplate webServiceTemplate;
    private String accessToken;

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

    private Object sendWithAuth(Object request) {
        return webServiceTemplate.marshalSendAndReceive(request, message -> {
            HttpUrlConnection conn = (HttpUrlConnection) TransportContextHolder.getTransportContext().getConnection();
            conn.addRequestHeader("Authorization", "Bearer " + accessToken);
        });
    }

    private CreateAuthorRequest buildCreateAuthorRequest(String key, String name) {
        Author author = new Author();
        author.setName(name);
        author.setDateOfBirth("1 Jan 1990");
        author.setCountryOfOrigin("TestLand");

        CreateAuthorRequest request = new CreateAuthorRequest();
        request.setIdempotencyKey(key);
        request.setAuthor(author);
        return request;
    }

    @Test
    public void newKey_createAuthor_succeeds() {
        logger.info("Starting newKey_createAuthor_succeeds");

        CreateAuthorRequest request = buildCreateAuthorRequest(UUID.randomUUID().toString(), "SOAP Idem Author A");
        CreateAuthorResponse response = (CreateAuthorResponse) sendWithAuth(request);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getMessage());

        logger.info("newKey_createAuthor_succeeds passed");
    }

    @Test
    public void sameKey_createAuthor_returnsSOAPFault() {
        logger.info("Starting sameKey_createAuthor_returnsSOAPFault");

        String key = UUID.randomUUID().toString();

        sendWithAuth(buildCreateAuthorRequest(key, "SOAP Idem Author B"));

        SoapFaultClientException fault = assertThrows(
                SoapFaultClientException.class,
                () -> sendWithAuth(buildCreateAuthorRequest(key, "SOAP Idem Author B")));

        assertTrue(fault.getMessage().contains("Duplicate request"));

        logger.info("sameKey_createAuthor_returnsSOAPFault passed");
    }

    @Test
    public void missingKey_createAuthor_returnsSOAPFault() {
        logger.info("Starting missingKey_createAuthor_returnsSOAPFault");

        Author author = new Author();
        author.setName("SOAP Idem Author C");
        author.setDateOfBirth("1 Jan 1990");
        author.setCountryOfOrigin("TestLand");

        CreateAuthorRequest request = new CreateAuthorRequest();
        // no idempotencyKey
        request.setAuthor(author);

        SoapFaultClientException fault = assertThrows(
                SoapFaultClientException.class,
                () -> sendWithAuth(request));

        assertTrue(fault.getMessage().contains("Idempotency-Key is required"));

        logger.info("missingKey_createAuthor_returnsSOAPFault passed");
    }

    @Test
    public void differentKeys_samePayload_bothSucceed() {
        logger.info("Starting differentKeys_samePayload_bothSucceed");

        CreateAuthorResponse r1 = (CreateAuthorResponse) sendWithAuth(
                buildCreateAuthorRequest(UUID.randomUUID().toString(), "SOAP Idem Author D"));
        CreateAuthorResponse r2 = (CreateAuthorResponse) sendWithAuth(
                buildCreateAuthorRequest(UUID.randomUUID().toString(), "SOAP Idem Author D"));

        assertEquals("SUCCESS", r1.getMessage());
        assertEquals("SUCCESS", r2.getMessage());

        logger.info("differentKeys_samePayload_bothSucceed passed");
    }

    @Test
    public void businessLogicFails_keyIsReleased_retryWithSameKeySucceeds() {
        logger.info("Starting businessLogicFails_keyIsReleased_retryWithSameKeySucceeds");

        String key = UUID.randomUUID().toString();

        // Attempt to create a book with a non-existent author → business failure
        Book bookWithMissingAuthor = new Book();
        bookWithMissingAuthor.setName("SOAP Retry Book");
        bookWithMissingAuthor.setPublicationYear("2024");
        bookWithMissingAuthor.setAuthorName("Non-existent SOAP Author 99");

        CreateBookRequest badRequest = new CreateBookRequest();
        badRequest.setIdempotencyKey(key);
        badRequest.setBook(bookWithMissingAuthor);

        // First attempt fails (author not found → BookValidationException SOAP fault)
        assertThrows(SoapFaultClientException.class, () -> sendWithAuth(badRequest));

        // Seed a valid author
        sendWithAuth(buildCreateAuthorRequest(UUID.randomUUID().toString(), "SOAP Retry Author"));

        // Retry with same key but valid author → should succeed (key was released on failure)
        Book goodBook = new Book();
        goodBook.setName("SOAP Retry Book Success");
        goodBook.setPublicationYear("2024");
        goodBook.setAuthorName("SOAP Retry Author");

        CreateBookRequest goodRequest = new CreateBookRequest();
        goodRequest.setIdempotencyKey(key);
        goodRequest.setBook(goodBook);

        CreateBookResponse response = (CreateBookResponse) sendWithAuth(goodRequest);
        assertNotNull(response);
        assertEquals("SUCCESS", response.getMessage());

        logger.info("businessLogicFails_keyIsReleased_retryWithSameKeySucceeds passed");
    }
}
