package com.library.mapper;

import com.library.dto.BookDTO;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Publisher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(source = "publisher.id", target = "publisherId")
    @Mapping(source = "authors", target = "authorIds", qualifiedByName = "mapAuthorsToAuthorIds")
    BookDTO toDTO(Book book);

    @Mapping(target = "authors", source = "authorIds", qualifiedByName = "mapAuthorIdsToAuthors")
    @Mapping(target = "publisher", source = "publisherId", qualifiedByName = "mapPublisherIdToPublisher")
    Book toEntity(BookDTO bookDTO);

    @Named("mapAuthorsToAuthorIds")
    default Set<Integer> mapAuthorsToAuthorIds(Set<Author> authors) {
        if (authors == null) return Collections.emptySet();
        return authors.stream()
                .map(Author::getId)
                .collect(Collectors.toSet());
    }

    @Named("mapAuthorIdsToAuthors")
    default Set<Author> mapAuthorIdsToAuthors(Set<Integer> authorIds) {
        if (authorIds == null) return Collections.emptySet();
        return authorIds.stream()
                .map(id -> {
                    Author author = new Author();
                    author.setId(id);
                    return author;
                })
                .collect(Collectors.toSet());
    }

    @Named("mapPublisherIdToPublisher")
    default Publisher mapPublisherIdToPublisher(Integer publisherId) {
        if (publisherId == null) return null;
        Publisher publisher = new Publisher();
        publisher.setId(publisherId);
        return publisher;
    }
}
