package com.messalas.omniapi.api.rest;

import com.messalas.omniapi.model.dto.AuthorDTO;
import com.messalas.omniapi.service.AuthorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rest")
public class AuthorRESTController {

    private final AuthorService authorService;

    public AuthorRESTController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping(path = "/createAuthor")
    public ResponseEntity<AuthorDTO> createAuthor(@RequestBody AuthorDTO authorDTO){
        authorService.createAuthor(authorDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/authors")
    public ResponseEntity<List<AuthorDTO>> getAllAuthors(){
        return ResponseEntity.ok(authorService.getAllAuthors());
    }

    @GetMapping("author/{id}")
    public ResponseEntity<AuthorDTO> getAuthor(@PathVariable Long id) { 
        return ResponseEntity.ok(authorService.getAuthorById(id));
    }

    @PutMapping("/authors/{id}")
    public ResponseEntity<AuthorDTO> updateAuthor(@PathVariable Long id, @RequestBody AuthorDTO authorDTO) {
        return ResponseEntity.ok(authorService.updateAuthor(id, authorDTO));
    }

    @DeleteMapping("/authors/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }

}
