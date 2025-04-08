package com.library.entity;

import java.util.*;

public class Book {
    private Integer id;
    private String title;
    private String publishedDate;
    private String genre;
    private Publisher publisher;
    private List<Author> authors = new ArrayList<>();

    public Book() {
    }

    public Book(Integer id, String title, String publishedDate, String genre, Publisher publisher, List<Author> authors) {
        this.id = id;
        this.title = title;
        this.publishedDate = publishedDate;
        this.genre = genre;
        this.publisher = publisher;
        this.authors = authors;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public void addAuthor(Author author) {
        authors.add(author);
        author.getBooks().add(this);
    }

    public void removeAuthor(Author author) {
        authors.remove(author);
        author.getBooks().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return  Objects.equals(id, book.id) &&
                Objects.equals(title, book.title) &&
                Objects.equals(publishedDate, book.publishedDate) &&
                Objects.equals(genre, book.genre) &&
                Objects.equals(publisher, book.publisher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, publishedDate, genre, publisher);
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", publishedDate='" + publishedDate + '\'' +
                ", genre='" + genre + '\'' +
                ", publisher=" + publisher +
                ", authors=" + authors +
                '}';
    }
}
