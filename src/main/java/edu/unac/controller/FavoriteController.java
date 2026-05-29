package edu.unac.controller;

import edu.unac.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    private static final String FAVORITES_PATH_PREFIX = "/api/favorites/";

    private final BookService bookService;

    public FavoriteController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> saveFavorite(@RequestBody Map<String, String> request) {
        String bookKey = request.getOrDefault("bookKey", request.get("key"));
        return save(bookKey);
    }

    @PostMapping({"/{bookKey}", "/**"})
    public ResponseEntity<Map<String, String>> saveFavorite(
            @PathVariable(required = false) String bookKey,
            HttpServletRequest request
    ) {
        String key = bookKey;
        String uri = request.getRequestURI();
        if (uri.startsWith(FAVORITES_PATH_PREFIX)) {
            key = uri.substring(FAVORITES_PATH_PREFIX.length());
        }
        return save(URLDecoder.decode(key, StandardCharsets.UTF_8));
    }

    private ResponseEntity<Map<String, String>> save(String bookKey) {
        bookService.saveFavorite(bookKey);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Libro guardado en favoritos exitosamente.");
        return ResponseEntity.ok(response);
    }
}
