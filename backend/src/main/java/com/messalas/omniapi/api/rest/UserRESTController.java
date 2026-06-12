package com.messalas.omniapi.api.rest;

import com.messalas.omniapi.model.dto.UserDetails;
import com.messalas.omniapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rest")
public class UserRESTController {

    private final UserService userService;

    public UserRESTController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Long> createUser(@RequestBody UserDetails userDetails) {
        Long id = userService.saveUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDetails> getUser(@PathVariable String username) {
        return ResponseEntity.ok(userService.loadUserByUsername(username));
    }

    @GetMapping
    public ResponseEntity<List<UserDetails>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}