package com.messalas.spring_boot_demo_A.controller;

import com.messalas.spring_boot_demo_A.model.dto.AuthorDTO;
import com.messalas.spring_boot_demo_A.model.entities.AuthorEntity;
import com.messalas.spring_boot_demo_A.service.AuthorService;
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
    public AuthorDTO createAuthor(@RequestBody AuthorEntity authorEntity){
        return authorService.createAuthor(authorEntity);
    }

    @GetMapping("/authors")
    public List<AuthorDTO> getAllAuthors(){
        return authorService.getAllAuthors();
    }

}
