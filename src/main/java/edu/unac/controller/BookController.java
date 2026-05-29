package edu.unac.controller;

import edu.unac.dto.BookResponse;
import edu.unac.dto.BookSearchRequest;
import edu.unac.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/search")
    public ResponseEntity<List<BookResponse>> searchBooks(@RequestBody BookSearchRequest request) {
        List<BookResponse> results = bookService.searchBooks(request);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/favorites")
    public ResponseEntity<Map<String, String>> saveFavorite(@RequestBody Map<String, String> request) {
        bookService.saveFavorite(request.getOrDefault("bookKey", request.get("key")));
        return ResponseEntity.ok(Map.of("message", "Libro agregado a favoritos."));
    }
}
