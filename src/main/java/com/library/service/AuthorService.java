package com.library.service;

import com.library.entity.Author;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface AuthorService {
    Set<Author> findAuthorsByBookId(int bookId);
    Map<Integer, Set<Author>> findAuthorsForBooks(Collection<Integer> bookIds);
}
