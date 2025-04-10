package com.library.repository;

import com.library.entity.Author;
import com.library.entity.Book;

import java.util.*;

public interface AuthorRepository {
    Optional<Author> findById(int id);
    List<Author> findAll();
    Author save(Author author);
    void delete(int id);
    Set<Book> findBooksByAuthorId(int authorId);
    Map<Integer, Set<Book>> findBooksForAuthors(Collection<Integer> authorIds);

}
