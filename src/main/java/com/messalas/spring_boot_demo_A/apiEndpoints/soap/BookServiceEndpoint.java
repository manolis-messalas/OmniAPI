package com.messalas.spring_boot_demo_A.apiEndpoints.soap;

import bookshelf.generated.CreateBookAuthorRequest;
import bookshelf.generated.CreateBookAuthorResponse;
import com.messalas.spring_boot_demo_A.model.dto.BookAuthorDTO;
import com.messalas.spring_boot_demo_A.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class BookServiceEndpoint {

    private static final String NAMESPACE_URI = "http://spring.io/guides/gs-producing-web-service";

    private final BookService bookService;

    @Autowired
    public BookServiceEndpoint(BookService bookService) {
        this.bookService = bookService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CreateBookAuthorRequest")
    @ResponsePayload
    public CreateBookAuthorResponse createBookAuthor(@RequestPayload CreateBookAuthorRequest request) {
        CreateBookAuthorResponse response = new CreateBookAuthorResponse();
        try {
            bookshelf.generated.BookAuthorDTO requestBookAuthorDTO = request.getBookAuthorDTO();
            BookAuthorDTO newBookAuthor = new BookAuthorDTO(
                    requestBookAuthorDTO.getBookName(), requestBookAuthorDTO.getDateOfBirth(), requestBookAuthorDTO.getCountryOfOrigin(), requestBookAuthorDTO.getAuthorName(), requestBookAuthorDTO.getPublicationYear()
            );
            bookService.saveBookAuthor(newBookAuthor);
            response.setSuccess(true);
        }catch (Exception e){
            System.err.println("Error saving book and author information:" + e.getMessage());
            response.setSuccess(false);
        }
        return response;
    }

}