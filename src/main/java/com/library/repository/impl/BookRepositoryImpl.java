package com.library.repository.impl;

import com.library.dto.BookAuthorDTO;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.repository.BookRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class BookRepositoryImpl implements BookRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Book> findById(int id) {
        Book book = entityManager.find(Book.class, id);
        return Optional.ofNullable(book);
    }

    @Override
    public List<Book> findAll() {
        TypedQuery<Book> query = entityManager.createQuery(
                "SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.authors LEFT JOIN FETCH b.publisher",
                Book.class);
        return query.getResultList();
    }

    @Override
    @Transactional
    public Book save(Book book) {
        if (book.getId() == null) {
            entityManager.persist(book);
            return book;
        } else {
            return entityManager.merge(book);
        }
    }

    @Override
    @Transactional
    public void delete(int id) {
        Book book = entityManager.find(Book.class, id);
        if (book != null) {
            book.getAuthors().forEach(author -> author.getBooks().remove(book));
            entityManager.remove(book);
        }
    }

    @Override
    public Set<Author> findAuthorsByBookId(int bookId) {
        TypedQuery<Author> query = entityManager.createQuery("""
            SELECT a FROM Author a
            JOIN a.books b
            WHERE b.id = :bookId
            """, Author.class);
        query.setParameter("bookId", bookId);
        return new HashSet<>(query.getResultList());
    }

    @Override
    public Map<Integer, Set<Author>> findAuthorsForBooks(Collection<Integer> bookIds) {
        if (bookIds.isEmpty()) {
            return Collections.emptyMap();
        }

        TypedQuery<BookAuthorDTO> query = entityManager.createQuery("""
            SELECT NEW com.library.dto.BookAuthorDTO(b.id, a) FROM Book b
            JOIN b.authors a
            WHERE b.id IN :bookIds
            """, BookAuthorDTO.class);
        query.setParameter("bookIds", bookIds);

        return query.getResultList().stream()
                .collect(Collectors.groupingBy(
                        BookAuthorDTO::bookId,
                        Collectors.mapping(
                                BookAuthorDTO::author,
                                Collectors.toSet()
                        )));
    }

    @Override
    @Transactional
    public Set<Book> findBooksByIds(Set<Integer> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Collections.emptySet();
        }

        TypedQuery<Book> query = entityManager.createQuery(
                "SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.authors WHERE b.id IN :bookIds",
                Book.class);
        query.setParameter("bookIds", bookIds);

        return new HashSet<>(query.getResultList());
    }
}