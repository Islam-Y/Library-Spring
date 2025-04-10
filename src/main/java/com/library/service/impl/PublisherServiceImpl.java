package com.library.service.impl;

import com.library.dto.PublisherDTO;
import com.library.entity.Book;
import com.library.entity.Publisher;
import com.library.exception.PublisherServiceException;
import com.library.mapper.PublisherMapper;
import com.library.repository.PublisherRepository;
import com.library.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PublisherServiceImpl implements PublisherService {
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

    @Autowired
    public PublisherServiceImpl(PublisherRepository publisherRepository, PublisherMapper publisherMapper) {
        this.publisherRepository = publisherRepository;
        this.publisherMapper = publisherMapper;
    }

    @Transactional(readOnly = true)
    public Set<PublisherDTO> getAllPublishers() {
        return publisherRepository.findAll().stream()
                .map(publisherMapper::toDTO)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Optional<PublisherDTO> getPublisherById(int id) {
        return publisherRepository.findById(id)
                .map(publisherMapper::toDTO);
    }

    @Transactional
    public void addPublisher(PublisherDTO publisherDTO) {
        if (publisherDTO.getName() == null || publisherDTO.getName().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        Publisher publisher = publisherMapper.toEntity(publisherDTO);

        Set<Book> books = Optional.ofNullable(publisherDTO.getBookIds())
                .orElse(Collections.emptySet())
                .stream()
                .map(bookId -> {
                    Book book = new Book();
                    book.setId(bookId);
                    return book;
                })
                .collect(Collectors.toSet());

        publisher.setBooks(books);

        try {
            publisherRepository.save(publisher);
        } catch (Exception e) {
            throw new PublisherServiceException("Error while adding publisher");
        }
    }

    @Transactional
    public void updatePublisher(int id, PublisherDTO publisherDTO) {
        Publisher existingPublisher = publisherRepository.findById(id)
                .orElseThrow(() -> new PublisherServiceException("Publisher not found"));

        existingPublisher.setName(publisherDTO.getName());

        Set<Book> books = Optional.ofNullable(publisherDTO.getBookIds())
                .orElse(Collections.emptySet())
                .stream()
                .map(bookId -> {
                    Book book = new Book();
                    book.setId(bookId);
                    return book;
                })
                .collect(Collectors.toSet());

        existingPublisher.setBooks(books);

        try {
            publisherRepository.save(existingPublisher);
        } catch (Exception e) {
            throw new PublisherServiceException("Error while updating publisher");
        }
    }

    @Transactional
    public void deletePublisher(int id) {
        if (!publisherRepository.findById(id).isPresent()) {
            throw new PublisherServiceException("Publisher not found");
        }

        try {
            publisherRepository.delete(id);
        } catch (Exception e) {
            throw new PublisherServiceException("Error while deleting publisher");
        }
    }
}
