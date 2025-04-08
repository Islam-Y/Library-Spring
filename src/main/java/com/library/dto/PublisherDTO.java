package com.library.dto;

import com.library.entity.Book;
import com.library.entity.Publisher;

import java.util.List;

public class PublisherDTO {
    private int id;
    private String name;
    private List<Integer> bookIds;

    public PublisherDTO() {}

    public PublisherDTO(Publisher publisher) {
       this.id = publisher.getId();
       this.name = publisher.getName();
       this.bookIds = publisher.getBooks().stream()
               .map(Book::getId)
               .toList();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getBookIds() {
        return bookIds;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBookIds(List<Integer> bookIds) {
        this.bookIds = bookIds;
    }

}
