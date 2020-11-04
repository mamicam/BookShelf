package com.example.bookshelf.type;

public class Book {
    private long id;
    private String title;
    private String author;
    private Integer pagesSum;
    private Integer yearOfPublished;
    private String publishingHouse;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getPagesSum() {
        return pagesSum;
    }

    public void setPagesSum(Integer pagesSum) {
        this.pagesSum = pagesSum;
    }

    public Integer getYearOfPublished() {
        return yearOfPublished;
    }

    public void setYearOfPublished(Integer yearOfPublished) {
        this.yearOfPublished = yearOfPublished;
    }

    public String getPublishingHouse() {
        return publishingHouse;
    }

    public void setPublishingHouse(String publishingHouse) {
        this.publishingHouse = publishingHouse;
    }
}