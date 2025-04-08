package com.library.service;

import com.library.entity.Book;

import java.util.List;
import java.util.Optional;

public interface BookService {
    Optional<Book> findById(int id);
    List<Book> findAll();
}
