package com.library.repository;

import com.library.entity.Book;
import com.library.entity.Publisher;

import java.util.*;

public interface PublisherRepository {
    Optional<Publisher> findById(int id);
    List<Publisher> findAll();
    Publisher save(Publisher publisher);
    void delete(int id);
    List<Book> findBooksByPublisherId(int publisherId);
    Map<Integer, List<Book>> findBooksForPublishers(Collection<Integer> publisherIds);
}
