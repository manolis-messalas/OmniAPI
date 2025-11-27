package com.messalas.spring_boot_demo_A.api.soap;

import bookshelf.generated.*;
import com.messalas.spring_boot_demo_A.exceptions.AuthorServiceException;
import com.messalas.spring_boot_demo_A.exceptions.AuthorValidationException;
import com.messalas.spring_boot_demo_A.model.dto.AuthorDTO;
import com.messalas.spring_boot_demo_A.service.AuthorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;
import java.util.stream.Collectors;

public class AuthorSOAPController {

    private static final String NAMESPACE_URI = "http://spring.io/guides/gs-producing-web-service";

    private static final Logger log = LoggerFactory.getLogger(AuthorSOAPController.class);

    private AuthorService authorService;

    @Autowired
    public AuthorSOAPController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CreateAuthorRequest")
    @ResponsePayload
    public CreateAuthorResponse createAuthor(@RequestPayload CreateAuthorRequest request) {
        CreateAuthorResponse response = new CreateAuthorResponse();
        try {
            bookshelf.generated.Author requestAuthor = request.getAuthor();
            AuthorDTO newAuthor = new AuthorDTO(
                    requestAuthor.getName(), requestAuthor.getDateOfBirth(), requestAuthor.getCountryOfOrigin()
            );
            Long authorId = authorService.createAuthor(newAuthor);
            response.setAuthorId(authorId);
            response.setMessage("SUCCESS");
            return response;
        } catch (IllegalArgumentException e) {
                throw new AuthorValidationException("Validation failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to create author", e);
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
                    soapAuthor.setName(authorDTO.getAuthorName());
                    soapAuthor.setDateOfBirth(authorDTO.getDateOfBirth());
                    soapAuthor.setCountryOfOrigin(authorDTO.getCountryOfOrigin());
                    return soapAuthor;
                })
                .collect(Collectors.toList());
    }

}