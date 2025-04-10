package com.library.repository;

import com.library.entity.Book;
import com.library.entity.Publisher;

import java.util.*;

public interface PublisherRepository {
    Optional<Publisher> findById(int id);
    Set<Publisher> findAll();
    Publisher save(Publisher publisher);
    void delete(int id);
    Set<Book> findBooksByPublisherId(int publisherId);
    Map<Integer, Set<Book>> findBooksForPublishers(Collection<Integer> publisherIds);
}
