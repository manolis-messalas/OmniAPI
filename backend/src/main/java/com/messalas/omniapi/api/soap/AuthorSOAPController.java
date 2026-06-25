package com.messalas.omniapi.api.soap;

import bookshelf.generated.*;
import com.messalas.omniapi.exceptions.AuthorServiceException;
import com.messalas.omniapi.exceptions.AuthorValidationException;
import com.messalas.omniapi.exceptions.OptimisticLockConflictException;
import com.messalas.omniapi.model.dto.AuthorDTO;
import com.messalas.omniapi.service.AuthorService;
import com.messalas.omniapi.service.IdempotencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;
import java.util.stream.Collectors;

@Endpoint
public class AuthorSOAPController {

    private static final String NAMESPACE_URI = "http://spring.io/guides/gs-producing-web-service";

    private static final Logger log = LoggerFactory.getLogger(AuthorSOAPController.class);

    private final AuthorService authorService;
    private final IdempotencyService idempotencyService;

    @Autowired
    public AuthorSOAPController(AuthorService authorService, IdempotencyService idempotencyService) {
        this.authorService = authorService;
        this.idempotencyService = idempotencyService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CreateAuthorRequest")
    @ResponsePayload
    public CreateAuthorResponse createAuthor(@RequestPayload CreateAuthorRequest request) {
        String key = request.getIdempotencyKey();
        if (key == null || key.isBlank()) {
            throw new AuthorValidationException("Idempotency-Key is required");
        }
        idempotencyService.registerKey(key);
        try {
            bookshelf.generated.Author requestAuthor = request.getAuthor();
            AuthorDTO newAuthor = new AuthorDTO(
                    requestAuthor.getName(), requestAuthor.getDateOfBirth(), requestAuthor.getCountryOfOrigin()
            );
            Long authorId = authorService.createAuthor(newAuthor);
            CreateAuthorResponse response = new CreateAuthorResponse();
            response.setAuthorId(authorId);
            response.setMessage("SUCCESS");
            return response;
        } catch (IllegalArgumentException e) {
            idempotencyService.deleteKey(key);
            throw new AuthorValidationException("Validation failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to create author", e);
            idempotencyService.deleteKey(key);
            throw new AuthorServiceException("Internal server error");
        }
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "UpdateAuthorRequest")
    @ResponsePayload
    public UpdateAuthorResponse updateAuthor(@RequestPayload UpdateAuthorRequest request) {
        try {
            bookshelf.generated.Author requestAuthor = request.getAuthor();
            AuthorDTO authorDTO = AuthorDTO.builder()
                    .version(requestAuthor.getVersion())
                    .authorName(requestAuthor.getName())
                    .dateOfBirth(requestAuthor.getDateOfBirth())
                    .countryOfOrigin(requestAuthor.getCountryOfOrigin())
                    .build();
            AuthorDTO updated = authorService.updateAuthor(requestAuthor.getId(), authorDTO);
            UpdateAuthorResponse response = new UpdateAuthorResponse();
            response.setAuthor(mapAuthorDTOToRequestDTO(updated));
            return response;
        } catch (OptimisticLockConflictException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new AuthorValidationException("Validation failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to update author", e);
            throw new AuthorServiceException("Internal server error");
        }
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeleteAuthorRequest")
    @ResponsePayload
    public DeleteAuthorResponse deleteAuthor(@RequestPayload DeleteAuthorRequest request) {
        DeleteAuthorResponse response = new DeleteAuthorResponse();
        try {
            authorService.deleteAuthor(request.getAuthorId());
            response.setStatus(true);
        } catch (Exception e) {
            response.setStatus(false);
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetAuthorRequest")
    @ResponsePayload
    public GetAuthorResponse getAuthor(@RequestPayload GetAuthorRequest request) {
        log.info("SOAP: Get author with ID: {}", request.getId());

        GetAuthorResponse response = new GetAuthorResponse();

        try {
            AuthorDTO authorDTO = authorService.getAuthorById(request.getId());
            response.setAuthor(mapAuthorDTOToRequestDTO(authorDTO));

        } catch (IllegalArgumentException e) {
            log.warn("Author not found: {}", e.getMessage());
            response.setAuthor(null);

        } catch (Exception e) {
            log.error("Error fetching author", e);
            response.setAuthor(null);
        }

        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetAuthorsRequest")
    @ResponsePayload
    public GetAuthorsResponse getAuthors(@RequestPayload GetAuthorsRequest request) {
        log.info("SOAP: Get all authors");

        GetAuthorsResponse response = new GetAuthorsResponse();

        try {
            List<AuthorDTO> authorDTOs = authorService.getAllAuthors();
            response.getAuthors().addAll(mapAuthorDTOsToRequestDTOs(authorDTOs));

        } catch (IllegalArgumentException e) {
            log.warn("Author not found: {}", e.getMessage());

        } catch (Exception e) {
            log.error("Error fetching author", e);
        }

        return response;
    }

    public Author mapAuthorDTOToRequestDTO(AuthorDTO authorDTO){
        Author soapAuthor = new Author();
        soapAuthor.setId(authorDTO.getAuthorId());
        soapAuthor.setVersion(authorDTO.getVersion());
        soapAuthor.setName(authorDTO.getAuthorName());
        soapAuthor.setDateOfBirth(authorDTO.getDateOfBirth());
        soapAuthor.setCountryOfOrigin(authorDTO.getCountryOfOrigin());
        return soapAuthor;
    }

    public List<Author> mapAuthorDTOsToRequestDTOs(List<AuthorDTO> authorDTOs) {
        return authorDTOs.stream()
                .map(authorDTO -> {
                    Author soapAuthor = new Author();
                    soapAuthor.setId(authorDTO.getAuthorId());
                    soapAuthor.setVersion(authorDTO.getVersion());
                    soapAuthor.setName(authorDTO.getAuthorName());
                    soapAuthor.setDateOfBirth(authorDTO.getDateOfBirth());
                    soapAuthor.setCountryOfOrigin(authorDTO.getCountryOfOrigin());
                    return soapAuthor;
                })
                .collect(Collectors.toList());
    }
}
