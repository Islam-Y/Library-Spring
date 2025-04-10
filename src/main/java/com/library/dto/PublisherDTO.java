package com.library.dto;

import java.util.Objects;
import java.util.Set;

public class PublisherDTO {
    private Integer id;
    private String name;
    private Set<Integer> bookIds;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Integer> getBookIds() {
        return bookIds;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBookIds(Set<Integer> bookIds) {
        this.bookIds = bookIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublisherDTO that = (PublisherDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(bookIds, that.bookIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, bookIds);
    }

    @Override
    public String toString() {
        return "PublisherDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", bookIds=" + bookIds +
                '}';
    }
}
