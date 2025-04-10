package com.library.service;

import com.library.dto.PublisherDTO;
import com.library.exception.PublisherServiceException;

import java.util.Optional;
import java.util.Set;

public interface PublisherService {
    Set<PublisherDTO> getAllPublishers();
    Optional<PublisherDTO> getPublisherById(int id) throws PublisherServiceException;
    void addPublisher(PublisherDTO publisherDTO) throws PublisherServiceException;
    void updatePublisher(int id, PublisherDTO publisherDTO) throws PublisherServiceException;
    void deletePublisher(int id) throws PublisherServiceException;
}
