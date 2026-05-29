package edu.unac.controller;

import edu.unac.dto.BookResponse;
import edu.unac.dto.BookSearchRequest;
import edu.unac.exception.BusinessRuleException;
import edu.unac.exception.GlobalExceptionHandler;
import edu.unac.service.BookService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({BookController.class, FavoriteController.class})
@Import(GlobalExceptionHandler.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void searchBooksReturnsHttpOkAndJsonBody() throws Exception {
        when(bookService.searchBooks(any(BookSearchRequest.class))).thenReturn(List.of(
                new BookResponse("/works/OL1W", "Clean Code", "Robert C. Martin", 2008, 12, "https://cover.test/1.jpg"),
                new BookResponse("/works/OL2W", "Clean Architecture", "Robert C. Martin", 2017, 5, null)
        ));

        mockMvc.perform(post("/api/books/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Clean","author":"Martin","language":"ingles","publishedAfter":2000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("/works/OL1W"))
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[0].author").value("Robert C. Martin"))
                .andExpect(jsonPath("$[0].publishedYear").value(2008))
                .andExpect(jsonPath("$[0].editions").value(12))
                .andExpect(jsonPath("$[0].coverUrl").value("https://cover.test/1.jpg"))
                .andExpect(jsonPath("$[1].title").value("Clean Architecture"));
    }

    @Test
    void searchBooksReturnsBadRequestForBusinessRuleErrors() throws Exception {
        when(bookService.searchBooks(any(BookSearchRequest.class)))
                .thenThrow(new BusinessRuleException("Debe enviar al menos el titulo o el autor."));

        mockMvc.perform(post("/api/books/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Debe enviar al menos el titulo o el autor."));
    }

    @Test
    void searchBooksReturnsInternalServerErrorForUnexpectedErrors() throws Exception {
        when(bookService.searchBooks(any(BookSearchRequest.class)))
                .thenThrow(new IllegalStateException("api unavailable"));

        mockMvc.perform(post("/api/books/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Clean\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error del servidor: api unavailable"));
    }

    @Test
    void saveFavoriteReturnsHttpOkAndMessage() throws Exception {
        mockMvc.perform(post("/api/books/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookKey\":\"/works/OL1W\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Libro agregado a favoritos."));

        verify(bookService).saveFavorite("/works/OL1W");
    }

    @Test
    void saveFavoriteAcceptsKeyFieldInBody() throws Exception {
        mockMvc.perform(post("/api/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"/works/OL2W\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Libro guardado en favoritos exitosamente."));

        verify(bookService).saveFavorite("/works/OL2W");
    }

    @Test
    void saveFavoriteReturnsBadRequestWhenServiceRejectsBookKey() throws Exception {
        doThrow(new BusinessRuleException("El identificador (key) del libro no puede estar vacio."))
                .when(bookService).saveFavorite(null);

        mockMvc.perform(post("/api/books/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El identificador (key) del libro no puede estar vacio."));
    }

    @Test
    void favoriteControllerSavesFavoriteFromPathVariable() throws Exception {
        mockMvc.perform(post("/api/favorites/OL1W"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Libro guardado en favoritos exitosamente."));

        verify(bookService).saveFavorite("OL1W");
    }

    @Test
    void favoriteControllerSupportsOpenLibraryKeysWithSlashes() throws Exception {
        mockMvc.perform(post("/api/favorites/works/OL3W"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Libro guardado en favoritos exitosamente."));

        verify(bookService).saveFavorite("works/OL3W");
    }
}
