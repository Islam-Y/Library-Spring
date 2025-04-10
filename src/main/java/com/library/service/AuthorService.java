package com.library.service;

import com.library.dto.AuthorDTO;
import com.library.exception.AuthorServiceException;

import java.util.Optional;
import java.util.Set;

public interface AuthorService {
    Set<AuthorDTO> getAllAuthors();
    Optional<AuthorDTO> getAuthorById(int id) throws AuthorServiceException;
    void addAuthor(AuthorDTO authorDTO) throws AuthorServiceException;
    void updateAuthor(int id, AuthorDTO authorDTO) throws AuthorServiceException;
    void deleteAuthor(int id) throws AuthorServiceException;
}
