package com.messalas.spring_boot_demo_A.apiEndpoints.rest;

import com.messalas.spring_boot_demo_A.model.dto.AuthorDTO;
import com.messalas.spring_boot_demo_A.service.AuthorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping(path = "/addAuthor")
    public ResponseEntity<AuthorDTO> createAuthor(@RequestBody AuthorDTO authorDTO){
        authorService.createAuthor(authorDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/authors")
    public ResponseEntity<List<AuthorDTO>> getAllAuthors(){
        return ResponseEntity.ok(authorService.getAllAuthors());
    }

}
