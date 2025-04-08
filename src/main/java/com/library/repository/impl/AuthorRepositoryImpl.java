package com.library.repository.impl;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.repository.AuthorRepository;
import com.library.repository.AuthorRowMapper;
import com.library.repository.BookRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
public class AuthorRepositoryImpl implements AuthorRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final AuthorRowMapper authorRowMapper;
    private final BookRowMapper bookRowMapper;
    private final AuthorRepository authorRepository;

    public AuthorRepositoryImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                AuthorRowMapper authorRowMapper, BookRowMapper bookRowMapper, AuthorRepository authorRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.authorRowMapper = authorRowMapper;
        this.bookRowMapper = bookRowMapper;
        this.authorRepository = authorRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Author> findById(int id) {
        String sql = "SELECT id, name, surname, country FROM authors WHERE id = ?";
        List<Author> authors = jdbcTemplate.query(sql, authorRowMapper, id);

        if (authors.isEmpty()) {
            return Optional.empty();
        }

        Author author = authors.get(0);
        author.setBooks(authorRepository.findBooksByAuthorId(id));
        return Optional.of(author);
    }

    @Transactional(readOnly = true)
    public List<Author> findAll() {
        String sql = "SELECT id, name, surname, country FROM authors";
        List<Author> authors = jdbcTemplate.query(sql, authorRowMapper);


        Map<Integer, Author> authorMap = new HashMap<>();
        authors.forEach(author -> authorMap.put(author.getId(), author));

        authorRepository.findBooksForAuthors(authorMap.keySet()).forEach((authorId, books) ->
                authorMap.get(authorId).setBooks(books));

        return authors;
    }

    @Transactional
    public Author save(Author author) {
        return author.getId() == null ? insert(author) : update(author);
    }

    @Transactional
    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM book_author WHERE author_id = ?", id);
        jdbcTemplate.update("DELETE FROM authors WHERE id = ?", id);
    }

    private Author insert(Author author) {
        String sql = "INSERT INTO authors (name, surname, country) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, author.getName());
            ps.setString(2, author.getSurname());
            ps.setString(3, author.getCountry());
            return ps;
        }, keyHolder);

        author.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        updateAuthorBooks(author);
        return author;
    }

    private Author update(Author author) {
        String sql = "UPDATE authors SET name = ?, surname = ?, country = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                author.getName(),
                author.getSurname(),
                author.getCountry(),
                author.getId());

        updateAuthorBooks(author);
        return author;
    }

    private void updateAuthorBooks(Author author) {
        jdbcTemplate.update("DELETE FROM book_author WHERE author_id = ?", author.getId());

        if (author.getBooks() != null && !author.getBooks().isEmpty()) {
            String sql = "INSERT INTO book_author (author_id, book_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(sql, author.getBooks().stream()
                    .map(book -> new Object[]{author.getId(), book.getId()})
                    .toList());
        }
    }

    @Transactional(readOnly = true)
    public List<Book> findBooksByAuthorId(int authorId) {
        String sql = """
                SELECT b.id, b.title 
                FROM books b 
                JOIN book_author ba ON b.id = ba.book_id 
                WHERE ba.author_id = ?""";

        return jdbcTemplate.query(sql, bookRowMapper, authorId)
                .stream()
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<Integer, List<Book>> findBooksForAuthors(Collection<Integer> authorIds) {
        if (authorIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String sql = """
            SELECT ba.author_id, b.id, b.title 
            FROM books b 
            JOIN book_author ba ON b.id = ba.book_id 
            WHERE ba.author_id IN (:authorIds)""";

        Map<String, Object> params = Collections.singletonMap("authorIds", authorIds);
        Map<Integer, List<Book>> result = new HashMap<>();

        namedParameterJdbcTemplate.query(sql, params, rs -> {
            int authorId = rs.getInt("author_id");
            Book book = new Book();
            book.setId(rs.getInt("id"));
            book.setTitle(rs.getString("title"));

            List<Book> books = result.computeIfAbsent(authorId, k -> new ArrayList<>());
            if (!containsBook(books, book)) {
                books.add(book);
            }
        });

        return result;
    }

    private boolean containsBook(List<Book> books, Book target) {
        for (Book book : books) {
            if (book.getId().equals(target.getId())) {
                return true;
            }
        }
        return false;
    }
}
