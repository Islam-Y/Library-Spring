package com.library.controller;

import com.library.dto.PublisherDTO;
import com.library.service.impl.PublisherServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/publishers")
public class PublisherController {

    private final PublisherServiceImpl publisherServiceImpl;

    public PublisherController(PublisherServiceImpl publisherServiceImpl) {
        this.publisherServiceImpl = publisherServiceImpl;
    }

    @GetMapping
    public ResponseEntity<List<PublisherDTO>> getAllPublishers() {
        List<PublisherDTO> publishers = publisherServiceImpl.getAllPublishers();
        return ResponseEntity.ok(publishers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublisherDTO> getPublisherById(@PathVariable int id) {
        return publisherServiceImpl.getPublisherById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> addPublisher(@RequestBody @Valid PublisherDTO publisher) {
        if (publisher.getName() == null || publisher.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        publisherServiceImpl.addPublisher(publisher);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePublisher(
            @PathVariable int id,
            @RequestBody @Valid PublisherDTO publisher) {

        if (publisher.getId() != id) {
            return ResponseEntity.badRequest().build();
        }

        publisherServiceImpl.updatePublisher(id, publisher);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublisher(@PathVariable int id) {
        publisherServiceImpl.deletePublisher(id);
        return ResponseEntity.noContent().build();
    }
}