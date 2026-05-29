package edu.unac.service.impl;

import edu.unac.client.OpenLibraryClient;
import edu.unac.dto.BookResponse;
import edu.unac.dto.BookSearchRequest;
import edu.unac.dto.OpenLibraryResponse;
import edu.unac.exception.BusinessRuleException;
import edu.unac.model.FavoriteBook;
import edu.unac.model.SearchHistory;
import edu.unac.repository.FavoriteBookRepository;
import edu.unac.repository.SearchHistoryRepository;
import edu.unac.service.BookService;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

    private static final List<String> VALID_LANGUAGES = List.of("ingles", "espanol", "portugues", "frances", "aleman");

    private final SearchHistoryRepository historyRepository;
    private final OpenLibraryClient openLibraryClient;
    private final FavoriteBookRepository favoriteRepository;

    public BookServiceImpl(
            SearchHistoryRepository historyRepository,
            OpenLibraryClient openLibraryClient,
            FavoriteBookRepository favoriteRepository
    ) {
        this.historyRepository = historyRepository;
        this.openLibraryClient = openLibraryClient;
        this.favoriteRepository = favoriteRepository;
    }

    @Override
    public List<BookResponse> searchBooks(BookSearchRequest request) {
        if ((request.getTitle() == null || request.getTitle().isBlank()) &&
                (request.getAuthor() == null || request.getAuthor().isBlank())) {
            throw new BusinessRuleException("Debe enviar al menos el titulo o el autor.");
        }

        int currentYear = LocalDate.now().getYear();
        if (request.getPublishedAfter() != null && request.getPublishedAfter() > currentYear) {
            throw new BusinessRuleException("El anio de publicacion no puede ser mayor al actual.");
        }

        if (request.getLanguage() != null && !VALID_LANGUAGES.contains(request.getLanguage().toLowerCase())) {
            throw new BusinessRuleException("Idioma no valido. Idiomas permitidos: " + VALID_LANGUAGES);
        }

        OpenLibraryResponse apiResponse = openLibraryClient.searchBooks(request.getTitle(), request.getAuthor());
        if (apiResponse == null || apiResponse.getDocs() == null) {
            throw new BusinessRuleException("Error al obtener datos de la API externa de libros.");
        }

        List<BookResponse> filteredBooks = apiResponse.getDocs().stream()
                .filter(doc -> doc.getFirstPublishYear() != null)
                .filter(doc -> request.getPublishedAfter() == null || doc.getFirstPublishYear() >= request.getPublishedAfter())
                .map(doc -> new BookResponse(
                        doc.getKey(),
                        doc.getTitle(),
                        doc.getAuthorName() != null && !doc.getAuthorName().isEmpty() ? doc.getAuthorName().get(0) : "Desconocido",
                        doc.getFirstPublishYear(),
                        doc.getEditionCount(),
                        doc.getCoverId() != null ? "https://covers.openlibrary.org/b/id/" + doc.getCoverId() + "-M.jpg" : null
                ))
                .collect(Collectors.toList());

        if (filteredBooks.size() < 3) {
            throw new BusinessRuleException("No se encontraron suficientes coincidencias. Minimo esperado: 3.");
        }

        SearchHistory history = new SearchHistory();
        history.setTitle(request.getTitle());
        history.setAuthor(request.getAuthor());
        history.setLanguage(request.getLanguage());
        history.setPublishedAfter(request.getPublishedAfter());
        historyRepository.save(history);

        return filteredBooks;
    }

    @Override
    public void saveFavorite(String bookKey) {
        if (bookKey == null || bookKey.isBlank() || "undefined".equalsIgnoreCase(bookKey) || "null".equalsIgnoreCase(bookKey)) {
            throw new BusinessRuleException("El identificador (key) del libro no puede estar vacio.");
        }

        if (favoriteRepository.existsByBookKey(bookKey)) {
            throw new BusinessRuleException("Este libro ya se encuentra en su lista de favoritos.");
        }

        FavoriteBook favorite = new FavoriteBook();
        favorite.setBookKey(bookKey);
        favoriteRepository.save(favorite);
    }
}
