package com.library.service.impl;

import com.library.exception.BookServiceException;
import com.library.entity.Publisher;
import com.library.repository.AuthorDAO;
import com.library.repository.impl.BookRepositoryImpl;
import com.library.dto.BookDTO;
import com.library.entity.Book;
import com.library.entity.Author;
import com.library.mapper.BookMapper;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;

public class BookServiceImpl {
    private final BookRepositoryImpl bookRepositoryImpl;
    private final BookMapper bookMapper;

    BookServiceImpl() {
        this.bookMapper = BookMapper.INSTANCE;
        this.bookRepositoryImpl = new BookRepositoryImpl();
    }

    private BookServiceImpl(BookRepositoryImpl bookRepositoryImpl, BookMapper mapper) {
        this.bookRepositoryImpl = bookRepositoryImpl;
        this.bookMapper = mapper;
    }

    public static BookServiceImpl forTest(BookRepositoryImpl bookRepositoryImpl, BookMapper bookMapper) {
        return new BookServiceImpl(bookRepositoryImpl, bookMapper);
    }

    public List<BookDTO> getAllBooks() {
        try {
            return bookRepositoryImpl.getAll().stream()
                    .map(bookMapper::toDTO)
                    .toList();
        } catch (SQLException e) {
            throw new BookServiceException("Error while getting list of books", e);
        }
    }

    public Optional<BookDTO> getBookById(int id) {
        try {
            return bookRepositoryImpl.getById(id)
                    .map(bookMapper::toDTO)
                    .orElseThrow(() -> new BookServiceException("Book not found", new RuntimeException()));

        } catch (SQLException e) {
            throw new BookServiceException("Error while getting book with ID " + id, e);
        }
    }

    public void addBook(BookDTO bookDTO) {
        AuthorDAO authorDAO = new AuthorDAO();
        try {
            if (bookDTO.getTitle() == null || bookDTO.getTitle().isEmpty()) {
                throw new IllegalArgumentException("Title is required");
            }

            Book book = bookMapper.toModel(bookDTO);
            Set<Author> authors = bookDTO.getAuthorIds().stream()
                    .map(authorId -> {
                        try {
                            return authorDAO.getById(authorId);
                        } catch (SQLException e) {
                            throw new BookServiceException("Error adding authors", new RuntimeException(e));
                        }
                    })
                    .flatMap(Optional::stream)
                    .collect(Collectors.toSet());

            book.setAuthors(authors);
            bookRepositoryImpl.create(book);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new BookServiceException("Foreign key constraint error: referenced publisher or author not found", e);
            }
            throw new BookServiceException("Error while adding book to database", e);
        }
    }


    public void updateBook(int id, BookDTO bookDTO) {
        try {
            if (bookDTO.getPublisherId() == null) {
                throw new IllegalArgumentException("Publisher ID is required");
            }

            Book existingBook = bookRepositoryImpl.getById(id)
                    .orElseThrow(() -> new BookServiceException("Book not found", new RuntimeException()));

            if (bookDTO.getPublisherId() == null) {
                throw new IllegalArgumentException("Publisher ID is required");
            }

            existingBook.setTitle(bookDTO.getTitle());
            existingBook.setPublishedDate(bookDTO.getPublishedDate());
            existingBook.setGenre(bookDTO.getGenre());

            Publisher publisher = new Publisher();
            publisher.setId(bookDTO.getPublisherId());
            existingBook.setPublisher(publisher);

            Set<Integer> authorIds = bookDTO.getAuthorIds() != null
                    ? bookDTO.getAuthorIds()
                    : Collections.emptySet();

            Set<Author> authors = authorIds.stream()
                    .map(authorId -> {
                        Author author = new Author();
                        author.setId(authorId);
                        return author;
                    })
                    .collect(Collectors.toSet());
            existingBook.setAuthors(authors);

            bookRepositoryImpl.update(existingBook);
        } catch (SQLException e) {
            throw new BookServiceException("Error while updating book with ID " + id, e);
        }
    }

    public void deleteBook(int id) {
        try {
            bookRepositoryImpl.delete(id);
        } catch (SQLException e) {
            throw new BookServiceException("Error while deleting book with ID " + id, e);
        }
    }
}