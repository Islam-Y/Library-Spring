package com.library.dto;

import com.library.entity.Book;

public record PublisherBookDTO(Integer publisherId, Book book) {}