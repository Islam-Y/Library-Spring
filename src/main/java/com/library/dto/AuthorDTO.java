package com.library.dto;

import java.util.Objects;
import java.util.Set;

public class AuthorDTO {
    private Integer id;
    private String name;
    private String surname;
    private String country;
    private Set<Integer> bookIds;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getCountry() {
        return country;
    }

    public Set<Integer> getBookIds() {
        return bookIds;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setBookIds(Set<Integer> bookIds) {
        this.bookIds = bookIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorDTO authorDTO = (AuthorDTO) o;
        return Objects.equals(id, authorDTO.id) && Objects.equals(name, authorDTO.name) && Objects.equals(surname, authorDTO.surname) && Objects.equals(country, authorDTO.country) && Objects.equals(bookIds, authorDTO.bookIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, surname, country, bookIds);
    }

    @Override
    public String toString() {
        return "AuthorDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", country='" + country + '\'' +
                ", bookIds=" + bookIds +
                '}';
    }
}
