package com.library.service.impl;

import com.library.dto.BookDTO;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Publisher;
import com.library.exception.BookServiceException;
import com.library.mapper.BookMapper;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookMapper bookMapper;

    public BookServiceImpl(BookRepository bookRepository,
                           AuthorRepository authorRepository,
                           BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.bookMapper = bookMapper;
    }

    @Transactional(readOnly = true)
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(bookMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<BookDTO> getBookById(int id) {
        return bookRepository.findById(id)
                .map(bookMapper::toDTO);
    }

    @Transactional
    public void addBook(BookDTO bookDTO) {
        if (bookDTO.getTitle() == null || bookDTO.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }

        Book book = bookMapper.toEntity(bookDTO);

        Set<Author> authors = Optional.ofNullable(bookDTO.getAuthorIds())
                .orElse(Collections.emptySet())
                .stream()
                .map(authorId -> authorRepository.findById(authorId)
                        .orElseThrow(() -> new BookServiceException("Author not found with ID: " + authorId)))
                .collect(Collectors.toSet());

        book.setAuthors(authors);
        authors.forEach(author -> author.getBooks().add(book));

        if (bookDTO.getPublisherId() != null) {
            Publisher publisher = new Publisher();
            publisher.setId(bookDTO.getPublisherId());
            book.setPublisher(publisher);
        }

        bookRepository.save(book);
    }

    @Transactional
    public void updateBook(int id, BookDTO bookDTO) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookServiceException("Book not found with ID: " + id));

        existingBook.setTitle(bookDTO.getTitle());
        existingBook.setPublishedDate(bookDTO.getPublishedDate());
        existingBook.setGenre(bookDTO.getGenre());

        if (bookDTO.getPublisherId() != null) {
            Publisher publisher = new Publisher();
            publisher.setId(bookDTO.getPublisherId());
            existingBook.setPublisher(publisher);
        }

        Set<Author> authors = Optional.ofNullable(bookDTO.getAuthorIds())
                .orElse(Collections.emptySet())
                .stream()
                .map(authorId -> authorRepository.findById(authorId)
                        .orElseThrow(() -> new BookServiceException("Author not found with ID: " + authorId)))
                .collect(Collectors.toSet());

        existingBook.setAuthors(authors);

        bookRepository.save(existingBook);
    }

    @Transactional
    public void deleteBook(int id) {
        if (bookRepository.findById(id).isEmpty()) {
            throw new BookServiceException("Book not found with ID: " + id);
        }
        bookRepository.delete(id);
    }
}
