package com.library.dto;

import java.util.Objects;
import java.util.Set;

public class BookDTO {
    private Integer id;
    private String title;
    private String publishedDate;
    private String genre;
    private Integer publisherId;
    private Set<Integer> authorIds;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public String getGenre() {
        return genre;
    }

    public Integer getPublisherId() {
        return publisherId;
    }

    public Set<Integer> getAuthorIds() {
        return authorIds;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setPublisherId(Integer publisherId) {
        this.publisherId = publisherId;
    }

    public void setAuthorIds(Set<Integer> authorIds) {
        this.authorIds = authorIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookDTO bookDTO = (BookDTO) o;
        return Objects.equals(id, bookDTO.id) && Objects.equals(title, bookDTO.title) && Objects.equals(publishedDate, bookDTO.publishedDate) && Objects.equals(genre, bookDTO.genre) && Objects.equals(publisherId, bookDTO.publisherId) && Objects.equals(authorIds, bookDTO.authorIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, publishedDate, genre, publisherId, authorIds);
    }

    @Override
    public String toString() {
        return "BookDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", publishedDate='" + publishedDate + '\'' +
                ", genre='" + genre + '\'' +
                ", publisherId=" + publisherId +
                ", authorIds=" + authorIds +
                '}';
    }
}
