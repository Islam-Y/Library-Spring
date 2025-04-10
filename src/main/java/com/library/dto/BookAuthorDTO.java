package com.library.dto;

import com.library.entity.Author;

public record BookAuthorDTO(Integer bookId, Author author) {}