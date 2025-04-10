package com.library.repository.impl;

import com.library.dto.AuthorBookDTO;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.repository.AuthorRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class AuthorRepositoryImpl implements AuthorRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Author> findById(int id) {
        Author author = entityManager.createQuery(
                        "SELECT a FROM Author a LEFT JOIN FETCH a.books WHERE a.id = :id", Author.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
        return Optional.ofNullable(author);
        }

    @Override
    public List<Author> findAll() {
        TypedQuery<Author> query = entityManager.createQuery(
                "SELECT a FROM Author a LEFT JOIN FETCH a.books", Author.class);
        return query.getResultList().stream().distinct().toList();
    }

    @Override
    @Transactional
    public Author save(Author author) {
        if (author == null) throw new IllegalArgumentException("Author cannot be null");
        if (author.getId() == null) {
            entityManager.persist(author);
            return author;
        } else {
            return entityManager.merge(author);
        }
    }

    @Override
    @Transactional
    public void delete(int id) {
        Author author = entityManager.find(Author.class, id);
        if (author != null) {
            if (!author.getBooks().isEmpty()) {
                author.getBooks().forEach(book -> book.getAuthors().remove(author));
            }
            entityManager.remove(author);
        }
    }

    @Override
    public Set<Book> findBooksByAuthorId(int authorId) {
        TypedQuery<Book> query = entityManager.createQuery("""
            SELECT b FROM Book b
            JOIN b.authors a
            WHERE a.id = :authorId
            """, Book.class);
        query.setParameter("authorId", authorId);
        return new HashSet<>(query.getResultList());
    }

    @Override
    public Map<Integer, Set<Book>> findBooksForAuthors(Collection<Integer> authorIds) {
        if (authorIds.isEmpty()) return Collections.emptyMap();

        TypedQuery<AuthorBookDTO> query = entityManager.createQuery("""
            SELECT NEW com.library.dto.AuthorBookDTO(a.id, b) FROM Author a
            JOIN a.books b
            WHERE a.id IN :authorIds
            """, AuthorBookDTO.class);
        query.setParameter("authorIds", authorIds);

        return query.getResultList().stream()
                .collect(Collectors.groupingBy(
                        AuthorBookDTO::authorId,
                        Collectors.mapping(
                                AuthorBookDTO::book,
                                Collectors.toSet()
                        )));
    }
}
