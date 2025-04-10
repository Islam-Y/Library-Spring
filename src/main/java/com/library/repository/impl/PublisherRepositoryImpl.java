package com.library.repository.impl;

import com.library.dto.PublisherBookDTO;
import com.library.entity.Book;
import com.library.entity.Publisher;
import com.library.repository.PublisherRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PublisherRepositoryImpl implements PublisherRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Publisher> findById(int id) {
        Publisher publisher = entityManager.find(Publisher.class, id);
        return Optional.ofNullable(publisher);
    }

    public Set<Publisher> findAll() {
        TypedQuery<Publisher> query = entityManager.createQuery(
                "SELECT DISTINCT p FROM Publisher p LEFT JOIN FETCH p.books ORDER BY p.id",
                Publisher.class);
        return new LinkedHashSet<>(query.getResultList());
    }

    @Override
    @Transactional
    public Publisher save(Publisher publisher) {
        if (publisher == null) {
            throw new IllegalArgumentException("Publisher cannot be null");
        }

        if (publisher.getId() == null) {
            entityManager.persist(publisher);
            return publisher;
        } else {
            return entityManager.merge(publisher);
        }
    }

    @Override
    @Transactional
    public void delete(int id) {
        Publisher publisher = entityManager.find(Publisher.class, id);
        if (publisher != null) {
            // Обнуляем ссылку у книг перед удалением
            publisher.getBooks().forEach(book -> book.setPublisher(null));
            entityManager.remove(publisher);
        }
    }

    @Override
    public Set<Book> findBooksByPublisherId(int publisherId) {
        TypedQuery<Book> query = entityManager.createQuery(
                "SELECT b FROM Book b WHERE b.publisher.id = :publisherId",
                Book.class);
        query.setParameter("publisherId", publisherId);
        return new HashSet<>(query.getResultList());
    }

    @Override
    public Map<Integer, Set<Book>> findBooksForPublishers(Collection<Integer> publisherIds) {
        if (publisherIds.isEmpty()) {
            return Collections.emptyMap();
        }

        TypedQuery<PublisherBookDTO> query = entityManager.createQuery("""
                 SELECT NEW com.library.dto.PublisherBookDTO(p.id, b)\s
                 FROM Publisher p
                 JOIN p.books b
                 WHERE p.id IN :publisherIds
                \s""", PublisherBookDTO.class);
        query.setParameter("publisherIds", publisherIds);

        return query.getResultList().stream()
                .collect(Collectors.groupingBy(
                        PublisherBookDTO::publisherId,
                        Collectors.mapping(
                                PublisherBookDTO::book,
                                Collectors.toSet()
                        )));
    }
}