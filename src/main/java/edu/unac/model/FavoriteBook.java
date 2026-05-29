package edu.unac.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorite_books")
public class FavoriteBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_key", nullable = false, unique = true)
    private String bookKey;

    @Column(name = "added_at")
    private LocalDateTime addedAt = LocalDateTime.now();

    // Constructor vacío requerido por JPA
    public FavoriteBook() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookKey() { return bookKey; }
    public void setBookKey(String bookKey) { this.bookKey = bookKey; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}