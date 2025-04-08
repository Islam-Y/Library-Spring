package com.library.service.impl;

import com.library.exception.PublisherServiceException;
import com.library.entity.Book;
import com.library.repository.impl.PublisherRepositoryImpl;
import com.library.dto.PublisherDTO;
import com.library.entity.Publisher;
import com.library.mapper.PublisherMapper;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PublisherServiceImpl {
    private final PublisherRepositoryImpl publisherRepositoryImpl;
    private final PublisherMapper publisherMapper;

    PublisherServiceImpl() {
        this.publisherRepositoryImpl = new PublisherRepositoryImpl();
        this.publisherMapper = PublisherMapper.INSTANCE;
    }

    private PublisherServiceImpl(PublisherRepositoryImpl publisherRepositoryImpl, PublisherMapper mapper) {
        this.publisherRepositoryImpl = publisherRepositoryImpl;
        this.publisherMapper = mapper;
    }

    public static PublisherServiceImpl forTest(PublisherRepositoryImpl publisherRepositoryImpl, PublisherMapper publisherMapper) {
        return new PublisherServiceImpl(publisherRepositoryImpl, publisherMapper);
    }

    public List<PublisherDTO> getAllPublishers() {
        try {
            return publisherRepositoryImpl.getAll().stream()
                    .map(publisherMapper::toDTO)
                    .toList();
        } catch (SQLException e) {
            throw new PublisherServiceException("Error while getting list of publishers", e);
        }
    }

    public Optional<PublisherDTO> getPublisherById(int id) {
        try {
            return publisherRepositoryImpl.getById(id)
                    .map(publisherMapper::toDTO)
                    .orElseThrow(() -> new PublisherServiceException("Publisher not found", new RuntimeException()));
        } catch (SQLException e) {
            throw new PublisherServiceException("Error while getting publisher with ID " + id, e);
        }
    }

    public void addPublisher(PublisherDTO publisherDTO) {
        Publisher publisher = publisherMapper.toModel(publisherDTO);
        if (publisherDTO.getName() == null || publisherDTO.getName().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        try {
            publisherRepositoryImpl.create(publisher);
            publisherRepositoryImpl.updatePublisherBooks(publisher.getId(), publisherDTO.getBookIds());

        } catch (SQLException e) {
            throw new PublisherServiceException("Error while adding publisher to database", e);
        }
    }

    public void updatePublisher(int id, PublisherDTO publisherDTO) {
        try {
            Publisher existingPublisher = publisherRepositoryImpl.getById(id)
                    .orElseThrow(() -> new PublisherServiceException("Publisher not found", new RuntimeException()));
            existingPublisher.setName(publisherDTO.getName());

            List<Integer> bookIds = publisherDTO.getBookIds() != null
                    ? publisherDTO.getBookIds()
                    : Collections.emptyList();

            List<Book> books = bookIds.stream()
                    .map(bookId -> {
                        Book book = new Book();
                        book.setId(bookId);
                        return book;
                    })
                    .toList();
            existingPublisher.setBooks(books);

            publisherRepositoryImpl.update(existingPublisher);
            publisherRepositoryImpl.updatePublisherBooks(id, publisherDTO.getBookIds());
        } catch (SQLException e) {
            throw new PublisherServiceException("Error while updating publisher with ID " + id, e);
        }
    }

    public void deletePublisher(int id) {
        try {
            publisherRepositoryImpl.delete(id);
        } catch (SQLException e) {
            throw new PublisherServiceException("Error while deleting publisher with ID " + id, e);
        }
    }
}