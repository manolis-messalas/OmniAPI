package com.messalas.omniapi.api.rest;

import com.messalas.omniapi.model.dto.AuthorDTO;
import com.messalas.omniapi.service.AuthorService;
import com.messalas.omniapi.service.IdempotencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rest")
public class AuthorRESTController {

    private final AuthorService authorService;
    private final IdempotencyService idempotencyService;

    public AuthorRESTController(AuthorService authorService, IdempotencyService idempotencyService) {
        this.authorService = authorService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping(path = "/createAuthor")
    public ResponseEntity<AuthorDTO> createAuthor(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody AuthorDTO authorDTO) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }
        idempotencyService.registerKey(idempotencyKey);
        try {
            authorService.createAuthor(authorDTO);
        } catch (Exception e) {
            idempotencyService.deleteKey(idempotencyKey);
            throw e;
        }
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
