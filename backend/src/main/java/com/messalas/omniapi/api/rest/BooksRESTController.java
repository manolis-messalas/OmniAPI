package com.messalas.omniapi.api.rest;

import com.messalas.omniapi.model.dto.BookAuthorDTO;
import com.messalas.omniapi.model.dto.BookDTO;
import com.messalas.omniapi.service.BookService;
import com.messalas.omniapi.service.IdempotencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rest")
public class BooksRESTController {

    private final BookService bookService;
    private final IdempotencyService idempotencyService;

    @Autowired
    public BooksRESTController(BookService bookService, IdempotencyService idempotencyService) {
        this.bookService = bookService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping("/addBookAuthor")
    public ResponseEntity<BookAuthorDTO> addBookAuthor(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody BookAuthorDTO bookAuthorDTO) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }
        idempotencyService.registerKey(idempotencyKey);
        try {
            bookService.saveBookAuthor(bookAuthorDTO);
        } catch (Exception e) {
            idempotencyService.deleteKey(idempotencyKey);
            throw e;
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/addBook")
    public ResponseEntity<BookDTO> addBook(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody BookDTO bookDTO) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }
        idempotencyService.registerKey(idempotencyKey);
        try {
            bookService.saveBook(bookDTO);
        } catch (Exception e) {
            idempotencyService.deleteKey(idempotencyKey);
            throw e;
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/books")
    public ResponseEntity<List<BookDTO>> getAllBooks(){
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/book/{name}")
    public ResponseEntity<BookDTO> getBook(@PathVariable String name) {
        return ResponseEntity.ok(bookService.findBookByName(name));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id, @RequestBody BookDTO bookDTO) {
        return ResponseEntity.ok(bookService.updateBook(id, bookDTO));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
