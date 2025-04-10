package com.library.dto;

import com.library.entity.Book;

public record AuthorBookDTO(Integer authorId, Book book) {}
