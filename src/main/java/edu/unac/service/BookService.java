package edu.unac.service;

import edu.unac.dto.BookResponse;
import edu.unac.dto.BookSearchRequest;
import java.util.List;

public interface BookService {
    void saveFavorite(String bookKey);
    List<BookResponse> searchBooks(BookSearchRequest request);
}