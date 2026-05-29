package edu.unac.dto;

public class BookResponse {
    private String key;
    private String title;
    private String author;
    private Integer publishedYear;
    private Integer editions;
    private String coverUrl;

    public BookResponse() {
    }

    public BookResponse(String title, String author, Integer publishedYear, Integer editions, String coverUrl) {
        this(null, title, author, publishedYear, editions, coverUrl);
    }

    public BookResponse(String key, String title, String author, Integer publishedYear, Integer editions, String coverUrl) {
        this.key = key;
        this.title = title;
        this.author = author;
        this.publishedYear = publishedYear;
        this.editions = editions;
        this.coverUrl = coverUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public Integer getPublishedYear() {
        return publishedYear;
    }

    public void setPublishedYear(Integer publishedYear) {
        this.publishedYear = publishedYear;
    }

    public Integer getEditions() {
        return editions;
    }

    public void setEditions(Integer editions) {
        this.editions = editions;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}
