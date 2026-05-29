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
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private SearchHistoryRepository historyRepository;

    @Mock
    private OpenLibraryClient openLibraryClient;

    @Mock
    private FavoriteBookRepository favoriteRepository;

    private BookServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BookServiceImpl(historyRepository, openLibraryClient, favoriteRepository);
    }

    @Test
    void searchBooksRejectsRequestWithoutTitleAndAuthor() {
        BookSearchRequest request = request("", "", "ingles", 2000);

        assertThatThrownBy(() -> service.searchBooks(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Debe enviar al menos el titulo o el autor.");

        verify(openLibraryClient, never()).searchBooks(any(), any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void searchBooksRejectsNullTitleAndNullAuthor() {
        BookSearchRequest request = request(null, null, null, null);

        assertThatThrownBy(() -> service.searchBooks(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Debe enviar al menos el titulo o el autor.");

        verify(openLibraryClient, never()).searchBooks(any(), any());
    }

    @Test
    void searchBooksRejectsFuturePublishedAfter() {
        BookSearchRequest request = request("Clean Code", null, "ingles", LocalDate.now().getYear() + 1);

        assertThatThrownBy(() -> service.searchBooks(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("El anio de publicacion no puede ser mayor al actual.");

        verify(openLibraryClient, never()).searchBooks(any(), any());
    }

    @Test
    void searchBooksRejectsInvalidLanguage() {
        BookSearchRequest request = request("Clean Code", null, "italiano", 2000);

        assertThatThrownBy(() -> service.searchBooks(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Idioma no valido");

        verify(openLibraryClient, never()).searchBooks(any(), any());
    }

    @Test
    void searchBooksRejectsNullApiResponse() {
        BookSearchRequest request = request("Clean Code", "Robert Martin", null, null);
        when(openLibraryClient.searchBooks("Clean Code", "Robert Martin")).thenReturn(null);

        assertThatThrownBy(() -> service.searchBooks(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Error al obtener datos de la API externa de libros.");

        verify(historyRepository, never()).save(any());
    }

    @Test
    void searchBooksRejectsResponseWithoutDocs() {
        BookSearchRequest request = request("Clean Code", null, null, null);
        when(openLibraryClient.searchBooks("Clean Code", null)).thenReturn(new OpenLibraryResponse());

        assertThatThrownBy(() -> service.searchBooks(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Error al obtener datos de la API externa de libros.");
    }

    @Test
    void searchBooksRejectsWhenThereAreLessThanThreeMatchesAfterFiltering() {
        BookSearchRequest request = request("Clean Code", null, null, 2010);
        OpenLibraryResponse response = response(
                doc("Old Book", "Author", 2000, 1, 10),
                doc("New Book", "Author", 2012, 1, 11),
                doc("No Year", "Author", null, 1, 12)
        );
        when(openLibraryClient.searchBooks("Clean Code", null)).thenReturn(response);

        assertThatThrownBy(() -> service.searchBooks(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No se encontraron suficientes coincidencias. Minimo esperado: 3.");

        verify(historyRepository, never()).save(any());
    }

    @Test
    void searchBooksMapsResultsFiltersByYearAndSavesHistory() {
        BookSearchRequest request = request("Clean Code", "Robert Martin", "ingles", 2008);
        OpenLibraryResponse response = response(
                doc("Too Old", "Someone", 2001, 1, 1),
                docWithKey("/works/OL1W", "Clean Code", "Robert C. Martin", 2008, 12, 100),
                doc("Clean Architecture", "Robert C. Martin", 2017, 5, 101),
                doc("Unknown Author", null, 2020, 2, null),
                docWithAuthors("Empty Author", List.of(), 2021, 3, 102)
        );
        when(openLibraryClient.searchBooks("Clean Code", "Robert Martin")).thenReturn(response);

        List<BookResponse> results = service.searchBooks(request);

        assertThat(results).hasSize(4);
        assertThat(results.get(0).getKey()).isEqualTo("/works/OL1W");
        assertThat(results.get(0).getTitle()).isEqualTo("Clean Code");
        assertThat(results.get(0).getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(results.get(0).getCoverUrl()).isEqualTo("https://covers.openlibrary.org/b/id/100-M.jpg");
        assertThat(results.get(2).getAuthor()).isEqualTo("Desconocido");
        assertThat(results.get(2).getCoverUrl()).isNull();
        assertThat(results.get(3).getAuthor()).isEqualTo("Desconocido");

        ArgumentCaptor<SearchHistory> historyCaptor = ArgumentCaptor.forClass(SearchHistory.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getTitle()).isEqualTo("Clean Code");
        assertThat(historyCaptor.getValue().getAuthor()).isEqualTo("Robert Martin");
        assertThat(historyCaptor.getValue().getLanguage()).isEqualTo("ingles");
        assertThat(historyCaptor.getValue().getPublishedAfter()).isEqualTo(2008);
    }

    @Test
    void searchBooksAllowsNullPublishedAfter() {
        BookSearchRequest request = request("Clean", null, null, null);
        OpenLibraryResponse response = response(
                doc("Book One", "Author", 1990, 1, null),
                doc("Book Two", "Author", 2000, 1, null),
                doc("Book Three", "Author", 2010, 1, null)
        );
        when(openLibraryClient.searchBooks("Clean", null)).thenReturn(response);

        List<BookResponse> results = service.searchBooks(request);

        assertThat(results).extracting(BookResponse::getTitle)
                .containsExactly("Book One", "Book Two", "Book Three");
        verify(historyRepository).save(any(SearchHistory.class));
    }

    @Test
    void saveFavoriteRejectsBlankBookKey() {
        assertThatThrownBy(() -> service.saveFavorite(" "))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("El identificador (key) del libro no puede estar vacio.");

        verify(favoriteRepository, never()).existsByBookKey(any());
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void saveFavoriteRejectsNullBookKey() {
        assertThatThrownBy(() -> service.saveFavorite(null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("El identificador (key) del libro no puede estar vacio.");

        verify(favoriteRepository, never()).existsByBookKey(any());
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void saveFavoriteRejectsDuplicates() {
        when(favoriteRepository.existsByBookKey("/works/OL1W")).thenReturn(true);

        assertThatThrownBy(() -> service.saveFavorite("/works/OL1W"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Este libro ya se encuentra en su lista de favoritos.");

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void saveFavoriteStoresBookKey() {
        when(favoriteRepository.existsByBookKey("/works/OL1W")).thenReturn(false);

        service.saveFavorite("/works/OL1W");

        ArgumentCaptor<FavoriteBook> favoriteCaptor = ArgumentCaptor.forClass(FavoriteBook.class);
        verify(favoriteRepository).save(favoriteCaptor.capture());
        assertThat(favoriteCaptor.getValue().getBookKey()).isEqualTo("/works/OL1W");
    }

    private static BookSearchRequest request(String title, String author, String language, Integer publishedAfter) {
        BookSearchRequest request = new BookSearchRequest();
        request.setTitle(title);
        request.setAuthor(author);
        request.setLanguage(language);
        request.setPublishedAfter(publishedAfter);
        return request;
    }

    private static OpenLibraryResponse response(OpenLibraryResponse.Doc... docs) {
        OpenLibraryResponse response = new OpenLibraryResponse();
        response.setDocs(List.of(docs));
        return response;
    }

    private static OpenLibraryResponse.Doc doc(String title, String author, Integer year, Integer editions, Integer coverId) {
        return docWithAuthors(title, author == null ? null : List.of(author), year, editions, coverId);
    }

    private static OpenLibraryResponse.Doc docWithKey(
            String key,
            String title,
            String author,
            Integer year,
            Integer editions,
            Integer coverId
    ) {
        OpenLibraryResponse.Doc doc = docWithAuthors(title, author == null ? null : List.of(author), year, editions, coverId);
        doc.setKey(key);
        return doc;
    }

    private static OpenLibraryResponse.Doc docWithAuthors(
            String title,
            List<String> authors,
            Integer year,
            Integer editions,
            Integer coverId
    ) {
        OpenLibraryResponse.Doc doc = new OpenLibraryResponse.Doc();
        doc.setKey("/works/OL" + Math.abs(title.hashCode()) + "W");
        doc.setTitle(title);
        doc.setAuthorName(authors);
        doc.setFirstPublishYear(year);
        doc.setEditionCount(editions);
        doc.setCoverId(coverId);
        return doc;
    }
}
