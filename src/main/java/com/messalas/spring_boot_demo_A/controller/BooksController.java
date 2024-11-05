package com.messalas.spring_boot_demo_A.controller;

import com.messalas.spring_boot_demo_A.model.dto.BookAuthorDTO;
import com.messalas.spring_boot_demo_A.model.dto.BookDTO;
import com.messalas.spring_boot_demo_A.model.entities.AuthorEntity;
import com.messalas.spring_boot_demo_A.service.BookInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BooksController {

    private final BookInfoService bookInfoService;

    @Autowired
    public BooksController(BookInfoService bookInfoService){
        this.bookInfoService = bookInfoService;
    }

    @PostMapping("/addBookAuthor")
    public ResponseEntity<BookAuthorDTO> createBookAuthor(@RequestBody BookAuthorDTO bookAuthorDTO){
        bookInfoService.saveBookAuthor(bookAuthorDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/books")
    public List<BookDTO> getAllBooks(){
        return bookInfoService.getAllBooks();
    }


}
