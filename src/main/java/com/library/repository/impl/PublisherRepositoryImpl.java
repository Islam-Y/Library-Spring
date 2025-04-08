package com.library.repository.impl;

import com.library.entity.Book;
import com.library.entity.Publisher;
import com.library.repository.PublisherRepository;
import com.library.repository.BookRowMapper;
import com.library.repository.PublisherRowMapper;
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
public class PublisherRepositoryImpl implements PublisherRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final PublisherRowMapper publisherRowMapper;
    private final BookRowMapper bookRowMapper;
    private final PublisherRepository publisherRepository;

    public PublisherRepositoryImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                                   PublisherRowMapper publisherRowMapper, BookRowMapper bookRowMapper, PublisherRepository publisherRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.publisherRowMapper = publisherRowMapper;
        this.bookRowMapper = bookRowMapper;
        this.publisherRepository = publisherRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Publisher> findById(int id) {
        String sql = "SELECT id, name FROM publishers WHERE id = ?";
        List<Publisher> publishers = jdbcTemplate.query(sql, publisherRowMapper, id);

        if (publishers.isEmpty()) {
            return Optional.empty();
        }

        Publisher publisher = publishers.getFirst();
        publisher.setBooks(publisherRepository.findBooksByPublisherId(id));
        return Optional.of(publisher);
    }

    @Transactional(readOnly = true)
    public List<Publisher> findAll() {
        String sql = "SELECT id, name FROM publishers";
        List<Publisher> publishers = jdbcTemplate.query(sql, publisherRowMapper);

        List<Integer> publisherIds = publishers.stream()
                .map(Publisher::getId)
                .toList();

        Map<Integer, List<Book>> booksByPublisherId =
                publisherRepository.findBooksForPublishers(publisherIds);

        publishers.forEach(publisher -> {
            List<Book> books = booksByPublisherId.get(publisher.getId());
            publisher.setBooks(books != null ? books : Collections.emptyList());
        });

        return publishers;
    }

    @Transactional
    public Publisher save(Publisher publisher) {
        return publisher.getId() == null ? insert(publisher) : update(publisher);
    }

    @Transactional
    public void delete(int id) {
        jdbcTemplate.update("UPDATE books SET publisher_id = NULL WHERE publisher_id = ?", id);
        jdbcTemplate.update("DELETE FROM publishers WHERE id = ?", id);
    }

    private Publisher insert(Publisher publisher) {
        String sql = "INSERT INTO publishers (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, publisher.getName());
            return ps;
        }, keyHolder);

        publisher.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return publisher;
    }

    private Publisher update(Publisher publisher) {
        String sql = "UPDATE publishers SET name = ? WHERE id = ?";
        jdbcTemplate.update(sql, publisher.getName(), publisher.getId());
        return publisher;
    }

    @Transactional(readOnly = true)
    public List<Book> findBooksByPublisherId(int publisherId) {
        String sql = """
                SELECT b.id, b.title 
                FROM books b 
                WHERE b.publisher_id = ?""";

        return new ArrayList<>(jdbcTemplate.query(sql, bookRowMapper, publisherId));
    }

    @Transactional(readOnly = true)
    public Map<Integer, List<Book>> findBooksForPublishers(Collection<Integer> publisherIds) {
        if (publisherIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String sql = """
                SELECT b.publisher_id, b.id, b.title 
                FROM books b 
                WHERE b.publisher_id IN (:publisherIds)""";

        Map<String, Object> params = Collections.singletonMap("publisherIds", publisherIds);
        Map<Integer, List<Book>> result = new HashMap<>();

        namedParameterJdbcTemplate.query(sql, params, rs -> {
            int publisherId = rs.getInt("publisher_id");
            Book book = new Book();
            book.setId(rs.getInt("id"));
            book.setTitle(rs.getString("title"));

            result.computeIfAbsent(publisherId, k -> new ArrayList<>()).add(book);
        });

        return result;
    }
}