package com.library.service.impl;

import com.library.dto.AuthorDTO;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.exception.AuthorServiceException;
import com.library.mapper.AuthorMapper;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final BookRepository bookRepository;

    @Autowired
    public AuthorServiceImpl(AuthorRepository authorRepository, AuthorMapper authorMapper, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.authorMapper = authorMapper;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public Set<AuthorDTO> getAllAuthors() {
        return authorRepository.findAll().stream()
                .map(authorMapper::toDto)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Optional<AuthorDTO> getAuthorById(int id) {
        return authorRepository.findById(id)
                .map(authorMapper::toDto);
    }

    @Transactional
    public void addAuthor(AuthorDTO authorDTO) {
        validateAuthor(authorDTO);

        Author author = authorMapper.toEntity(authorDTO);
        setBooksFromIds(author, authorDTO.getBookIds());

        authorRepository.save(author);
    }

    @Transactional
    public void updateAuthor(int id, AuthorDTO authorDTO) {
        validateAuthor(authorDTO);

        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new AuthorServiceException("Author not found"));

        authorMapper.updateEntity(authorDTO, existingAuthor);
        setBooksFromIds(existingAuthor, authorDTO.getBookIds());

        authorRepository.save(existingAuthor);
    }

    @Transactional
    public void deleteAuthor(int id) {
        if (!authorRepository.findById(id).isPresent()) {
            throw new AuthorServiceException("Author not found");
        }

        authorRepository.delete(id);
    }

    private void validateAuthor(AuthorDTO authorDTO) {
        if (authorDTO.getName() == null || authorDTO.getName().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
    }

    private void setBooksFromIds(Author author, Set<Integer> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            author.getBooks().forEach(book -> book.getAuthors().remove(author));
            author.getBooks().clear();
            return;
        }

        Set<Book> books = bookRepository.findBooksByIds(bookIds);

        if (books.size() != bookIds.size()) {
            Set<Integer> missingIds = bookIds.stream()
                    .filter(id -> books.stream().noneMatch(b -> b.getId().equals(id)))
                    .collect(Collectors.toSet());
            throw new AuthorServiceException("Books not found with IDs: " + missingIds);
        }

        author.getBooks().forEach(book -> {
            if (!books.contains(book)) {
                book.getAuthors().remove(author);
            }
        });

        books.forEach(book -> {
            if (!author.getBooks().contains(book)) {
                book.getAuthors().add(author);
            }
        });

        author.setBooks(books);
    }
}
