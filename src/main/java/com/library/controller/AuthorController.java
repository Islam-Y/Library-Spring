package com.library.controller;

import com.library.dto.AuthorDTO;
import com.library.service.impl.AuthorServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/authors")
public class AuthorController {

    private final AuthorServiceImpl authorServiceImpl;

    @Autowired
    public AuthorController(AuthorServiceImpl authorServiceImpl) {
        this.authorServiceImpl = authorServiceImpl;
    }

    @GetMapping
    public ResponseEntity<Set<AuthorDTO>> getAllAuthors() {
        Set<AuthorDTO> authors = authorServiceImpl.getAllAuthors();
        return ResponseEntity.ok(authors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorDTO> getAuthorById(@PathVariable int id) {
        return authorServiceImpl.getAuthorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> addAuthor(@RequestBody @Valid AuthorDTO author) {
        if (author.getName() == null || author.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        authorServiceImpl.addAuthor(author);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateAuthor(
            @PathVariable int id,
            @RequestBody @Valid AuthorDTO author) {

        if (author.getId() != id) {
            return ResponseEntity.badRequest().build();
        }

        authorServiceImpl.updateAuthor(id, author);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable int id) {
        authorServiceImpl.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}