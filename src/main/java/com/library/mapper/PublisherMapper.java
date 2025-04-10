package com.library.mapper;

import com.library.dto.PublisherDTO;
import com.library.entity.Book;
import com.library.entity.Publisher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PublisherMapper {

    @Mapping(source = "books", target = "bookIds", qualifiedByName = "mapBooksToBookIds")
    PublisherDTO toDTO(Publisher publisher);

    @Mapping(source = "bookIds", target = "books", qualifiedByName = "mapBookIdsToBooks")
    Publisher toEntity(PublisherDTO publisherDTO);

    @Named("mapBooksToBookIds")
    default Set<Integer> mapBooksToBookIds(Set<Book> books) {
        if (books == null) {
            return Collections.emptySet();
        }
        return books.stream()
                .map(Book::getId)
                .collect(Collectors.toSet());
    }

    @Named("mapBookIdsToBooks")
    default Set<Book> mapBookIdsToBooks(Set<Integer> bookIds) {
        if (bookIds == null) {
            return Collections.emptySet();
        }
        return bookIds.stream()
                .map(id -> {
                    Book book = new Book();
                    book.setId(id);
                    return book;
                })
                .collect(Collectors.toSet());
    }
}