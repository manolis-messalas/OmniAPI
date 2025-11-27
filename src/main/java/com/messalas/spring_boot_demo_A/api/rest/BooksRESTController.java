package com.messalas.spring_boot_demo_A.api.rest;

import com.messalas.spring_boot_demo_A.model.dto.BookAuthorDTO;
import com.messalas.spring_boot_demo_A.model.dto.BookDTO;
import com.messalas.spring_boot_demo_A.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rest")
public class BooksRESTController {

    private final BookService bookService;

    @Autowired
    public BooksRESTController(BookService bookService){
        this.bookService = bookService;
    }

    @PostMapping("/addBookAuthor")
    public ResponseEntity<BookAuthorDTO> createBookAuthor(@RequestBody BookAuthorDTO bookAuthorDTO){
        bookService.saveBookAuthor(bookAuthorDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/books")
    public ResponseEntity<List<BookDTO>> getAllBooks(){
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

}