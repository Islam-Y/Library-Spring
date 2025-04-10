package com.library.mapper;

import com.library.dto.AuthorDTO;
import com.library.entity.Author;
import com.library.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AuthorMapper {
    @Mapping(target = "bookIds", source = "books", qualifiedByName = "booksToIds")
    AuthorDTO toDto(Author author);

    @Mapping(target = "books", ignore = true)
    Author toEntity(AuthorDTO authorDTO);

    @Mapping(target = "books", ignore = true)
    void updateEntity(AuthorDTO authorDTO, @MappingTarget Author author);

    @Named("booksToIds")
    default Set<Integer> booksToIds(Set<Book> books) {
        if (books == null) return Collections.emptySet();
        return books.stream()
                .map(Book::getId)
                .collect(Collectors.toSet());
    }
}