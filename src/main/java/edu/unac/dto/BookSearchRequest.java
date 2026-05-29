package edu.unac.dto;

public class BookSearchRequest {
    private String title;
    private String author;
    private String language;
    private Integer publishedAfter;

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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getPublishedAfter() {
        return publishedAfter;
    }

    public void setPublishedAfter(Integer publishedAfter) {
        this.publishedAfter = publishedAfter;
    }
}
