package com.messalas.spring_boot_demo_A.api.soap;

import bookshelf.generated.*;
import com.messalas.spring_boot_demo_A.exceptions.BookServiceException;
import com.messalas.spring_boot_demo_A.exceptions.BookValidationException;
import com.messalas.spring_boot_demo_A.model.dto.BookAuthorDTO;
import com.messalas.spring_boot_demo_A.model.dto.BookDTO;
import com.messalas.spring_boot_demo_A.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;
import java.util.stream.Collectors;

@Endpoint
public class BookSOAPController {

    private static final String NAMESPACE_URI = "http://spring.io/guides/gs-producing-web-service";

    private static final Logger log = LoggerFactory.getLogger(BookSOAPController.class);


    private final BookService bookService;

    @Autowired
    public BookSOAPController(BookService bookService) {
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

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CreateBookRequest")
    @ResponsePayload
    public CreateBookResponse createBook(@RequestPayload CreateBookRequest request) {
        CreateBookResponse response = new CreateBookResponse();
        try {
            bookshelf.generated.Book requestBook = request.getBook();
            com.messalas.spring_boot_demo_A.model.dto.AuthorDTO authorDTO = com.messalas.spring_boot_demo_A.model.dto.AuthorDTO.builder()
                    .authorName(requestBook.getAuthorName())
                    .build();

            BookDTO newBook = BookDTO.builder()
                    .bookName(requestBook.getName())
                    .publicationYear(requestBook.getPublicationYear())
                    .authorDTO(authorDTO)
                    .build();

            Long bookId = bookService.saveBook(newBook);
            response.setBookId(bookId);
            response.setMessage("SUCCESS");
            return response;
        } catch (IllegalArgumentException e) {
            throw new BookValidationException("Validation failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to create book", e);
            throw new BookServiceException("Internal server error");
        }
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeleteBookRequest")
    @ResponsePayload
    public DeleteBookResponse deleteBook(@RequestPayload DeleteBookRequest request){
        DeleteBookResponse response = new DeleteBookResponse();
        try{
            Long bookId = request.getBookId();
            bookService.deleteBook(bookId);
            response.setStatus(true);
        } catch (Exception e){
            System.err.println("Error deleting book :" + e.getMessage());
            response.setStatus(false);
        }
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetBooksRequest")
    @ResponsePayload
    public GetBooksResponse getBooks(@RequestPayload GetBooksRequest request) {
        GetBooksResponse response = new GetBooksResponse();

        try {
            List<BookDTO> books = bookService.getAllBooks();

            response.getBooks().addAll(mapBookDTOsToRequestDTOs(books));

        } catch (Exception e) {
            log.error("Error fetching books", e);
        }

        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetBookRequest")
    @ResponsePayload
    public GetBookResponse getBook(@RequestPayload GetBookRequest request) {
        GetBookResponse response = new GetBookResponse();

        try{
            BookDTO bookDTO = bookService.findBookByName(request.getName());
            ResponseEntity.ok(mapBookDTOToRequestDTO(bookDTO));
        } catch (Exception e) {
            log.error("Error fetching book", e);
        }

        return response;
    }

    public Book mapBookDTOToRequestDTO(com.messalas.spring_boot_demo_A.model.dto.BookDTO bookDTO){
        Book soapBook = new Book();
        soapBook.setName(bookDTO.getBookName());
        soapBook.setPublicationYear(bookDTO.getPublicationYear());
        soapBook.setAuthorName(bookDTO.getAuthorDTO().getAuthorName());
        return soapBook;
    }

    public List<Book> mapBookDTOsToRequestDTOs(List<BookDTO> bookDTOs) {
        return bookDTOs.stream()
                .map(bookDTO -> {
                    Book soapBook = new Book();
                    soapBook.setName(bookDTO.getBookName());
                    soapBook.setPublicationYear(bookDTO.getPublicationYear());
                    soapBook.setAuthorName(bookDTO.getAuthorDTO().getAuthorName());
                    return soapBook;
                })
                .collect(Collectors.toList());
    }

}