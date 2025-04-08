package com.library.controller;

import com.library.dto.BookDTO;
import com.library.service.impl.BookServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookServiceImpl bookServiceImpl;

    public BookController(BookServiceImpl bookServiceImpl) {
        this.bookServiceImpl = bookServiceImpl;
    }

    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<BookDTO> books = bookServiceImpl.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable int id) {
        return bookServiceImpl.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> addBook(@RequestBody @Valid BookDTO book) {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        bookServiceImpl.addBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateBook(
            @PathVariable int id,
            @RequestBody @Valid BookDTO book) {

        if (book.getId() != id) {
            return ResponseEntity.badRequest().build();
        }

        bookServiceImpl.updateBook(id, book);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable int id) {
        bookServiceImpl.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}