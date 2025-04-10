package com.library.repository;

import com.library.entity.Author;
import com.library.entity.Book;

import java.util.*;

public interface BookRepository {
    Optional<Book> findById(int id);
    List<Book> findAll();
    Book save(Book book);
    void delete(int id);
    Set<Author> findAuthorsByBookId(int bookId);
    Map<Integer, Set<Author>> findAuthorsForBooks(Collection<Integer> bookIds);
    Set<Book> findBooksByIds(Set<Integer> bookIds);

}
