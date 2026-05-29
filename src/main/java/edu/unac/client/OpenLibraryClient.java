package edu.unac.client;

import edu.unac.dto.OpenLibraryResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.text.Normalizer;

@Component
public class OpenLibraryClient {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://openlibrary.org/search.json";

    public OpenLibraryClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Función para limpiar tildes y la letra 'ñ'
    private String sanitizeQuery(String text) {
        if (text == null) return null;
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    public OpenLibraryResponse searchBooks(String title, String author) {

        // CORRECCIÓN AQUÍ: Usamos fromUriString en lugar de fromHttpUrl
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL);

        if (title != null && !title.isBlank()) {
            builder.queryParam("title", sanitizeQuery(title));
        }
        if (author != null && !author.isBlank()) {
            builder.queryParam("author", sanitizeQuery(author));
        }

        URI uriCodificada = builder.build().encode().toUri();

        return restTemplate.getForObject(uriCodificada, OpenLibraryResponse.class);
    }
}