package com.library.repository.impl;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.repository.AuthorRowMapper;
import com.library.repository.BookRepository;
import com.library.repository.BookRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.*;

@Repository
public class BookRepositoryImpl implements BookRepository {

    private static final Logger logger = LoggerFactory.getLogger(BookRepositoryImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final BookRowMapper bookRowMapper;
    private final AuthorRowMapper authorRowMapper;
    private final BookRepository bookRepository;

    public BookRepositoryImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                              BookRowMapper bookRowMapper, AuthorRowMapper authorRowMapper, BookRepository bookRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.bookRowMapper = bookRowMapper;
        this.authorRowMapper = authorRowMapper;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Book> findById(int id) {
        String sql = """
                SELECT b.id, b.title, b.published_date, b.genre, 
                       p.id AS publisher_id, p.name AS publisher_name
                FROM books b
                LEFT JOIN publishers p ON b.publisher_id = p.id
                WHERE b.id = ?
                """;

        List<Book> books = jdbcTemplate.query(sql, bookRowMapper, id);

        if (books.isEmpty()) {
            return Optional.empty();
        }

        Book book = books.get(0);
        book.setAuthors(bookRepository.findAuthorsByBookId(id));
        return Optional.of(book);
    }

    @Transactional(readOnly = true)
    public List<Book> findAll() {
        String sql = """
                SELECT b.id, b.title, b.published_date, b.genre, 
                       p.id AS publisher_id, p.name AS publisher_name
                FROM books b
                LEFT JOIN publishers p ON b.publisher_id = p.id
                """;

        List<Book> books = jdbcTemplate.query(sql, bookRowMapper);

        Map<Integer, Book> bookMap = new HashMap<>();
        books.forEach(book -> bookMap.put(book.getId(), book));

        bookRepository.findAuthorsForBooks(bookMap.keySet()).forEach((bookId, authors) ->
                bookMap.get(bookId).setAuthors(authors));

        return books;
    }

    @Transactional
    public Book save(Book book) {
        return book.getId() == null ? insert(book) : update(book);
    }

    @Transactional
    public void delete(int id) {
        logger.info("Deleting book with id {}", id);
        jdbcTemplate.update("DELETE FROM book_author WHERE book_id = ?", id);
        jdbcTemplate.update("DELETE FROM books WHERE id = ?", id);
    }

    private Book insert(Book book) {
        String sql = "INSERT INTO books (title, published_date, publisher_id, genre) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            var ps = conn.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, book.getTitle());
            ps.setObject(2, book.getPublishedDate(), Types.DATE);
            ps.setObject(3, book.getPublisher() != null ? book.getPublisher().getId() : null, Types.INTEGER);
            ps.setString(4, book.getGenre());
            return ps;
        }, keyHolder);

        book.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        updateAuthorsForBook(book);
        return book;
    }

    private Book update(Book book) {
        String sql = "UPDATE books SET title = ?, published_date = ?, publisher_id = ?, genre = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                book.getTitle(),
                book.getPublishedDate(),
                book.getPublisher() != null ? book.getPublisher().getId() : null,
                book.getGenre(),
                book.getId());
        updateAuthorsForBook(book);
        return book;
    }

    private void updateAuthorsForBook(Book book) {
        jdbcTemplate.update("DELETE FROM book_author WHERE book_id = ?", book.getId());
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            String sql = "INSERT INTO book_author (book_id, author_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(sql, book.getAuthors().stream()
                    .map(author -> new Object[]{book.getId(), author.getId()})
                    .toList());
        }
    }

    @Transactional(readOnly = true)
    public List<Author> findAuthorsByBookId(int bookId) {
        String sql = """
                SELECT a.id, a.name, a.surname, a.country
                FROM authors a
                JOIN book_author ba ON a.id = ba.author_id
                WHERE ba.book_id = ?
                """;

        List<Author> authors = jdbcTemplate.query(sql, authorRowMapper, bookId);

        return authors.stream()
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<Integer, List<Author>> findAuthorsForBooks(Collection<Integer> bookIds) {
        if (bookIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String sql = """
                SELECT ba.book_id, a.id, a.name, a.surname, a.country
                FROM authors a
                JOIN book_author ba ON a.id = ba.author_id
                WHERE ba.book_id IN (:bookIds)
                """;

        Map<String, Object> params = Collections.singletonMap("bookIds", bookIds);
        Map<Integer, List<Author>> result = new HashMap<>();

        namedParameterJdbcTemplate.query(sql, params, rs -> {
            int bookId = rs.getInt("book_id");
            Author author = authorRowMapper.mapRow(rs, rs.getRow());

            List<Author> authors = result.computeIfAbsent(bookId, k -> new ArrayList<>());
            if (!containsAuthor(authors, author)) {
                authors.add(author);
            }
        });

        return result;
    }

    private boolean containsAuthor(List<Author> authors, Author target) {
        for (Author author : authors) {
            if (author.getId().equals(target.getId())) {
                return true;
            }
        }
        return false;
    }
}
