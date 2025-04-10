package com.library.service;

import com.library.dto.BookDTO;
import com.library.exception.BookServiceException;

import java.util.List;
import java.util.Optional;

public interface BookService {
    List<BookDTO> getAllBooks();
    Optional<BookDTO> getBookById(int id) throws BookServiceException;
    void addBook(BookDTO bookDTO) throws BookServiceException;
    void updateBook(int id, BookDTO bookDTO) throws BookServiceException;
    void deleteBook(int id) throws BookServiceException;
}
