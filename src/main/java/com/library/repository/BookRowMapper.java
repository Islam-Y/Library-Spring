package com.library.repository;

import com.library.entity.Book;
import com.library.entity.Publisher;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class BookRowMapper implements RowMapper<Book> {
    @Override
    public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setPublishedDate(Optional.ofNullable(rs.getDate("published_date"))
                .map(Date::toString).orElse(null));
        book.setGenre(rs.getString("genre"));

        int publisherId = rs.getInt("publisher_id");
        if (!rs.wasNull()) {
            Publisher publisher = new Publisher();
            publisher.setId(publisherId);
            publisher.setName(rs.getString("publisher_name"));
            book.setPublisher(publisher);
        }

        return book;
    }
}
