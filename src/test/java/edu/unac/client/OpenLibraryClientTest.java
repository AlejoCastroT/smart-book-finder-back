package edu.unac.client;

import edu.unac.dto.OpenLibraryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenLibraryClientTest {

    @Test
    void searchBooksCallsOpenLibraryWithTitleAndAuthorAndMapsResponse() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        OpenLibraryClient client = new OpenLibraryClient(restTemplate);

        server.expect(requestTo("https://openlibrary.org/search.json?title=clean%20code&author=martin"))
                .andRespond(withSuccess("""
                        {"docs":[{"key":"/works/OL1W","title":"Clean Code","author_name":["Robert C. Martin"],"first_publish_year":2008,"edition_count":12,"cover_i":123}]}
                        """, MediaType.APPLICATION_JSON));

        OpenLibraryResponse response = client.searchBooks("clean code", "martin");

        assertThat(response.getDocs()).hasSize(1);
        assertThat(response.getDocs().getFirst().getKey()).isEqualTo("/works/OL1W");
        assertThat(response.getDocs().getFirst().getTitle()).isEqualTo("Clean Code");
        assertThat(response.getDocs().getFirst().getAuthorName()).containsExactly("Robert C. Martin");
        assertThat(response.getDocs().getFirst().getFirstPublishYear()).isEqualTo(2008);
        assertThat(response.getDocs().getFirst().getEditionCount()).isEqualTo(12);
        assertThat(response.getDocs().getFirst().getCoverId()).isEqualTo(123);
        server.verify();
    }

    @Test
    void searchBooksDoesNotSendBlankQueryParameters() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        OpenLibraryClient client = new OpenLibraryClient(restTemplate);

        server.expect(requestTo("https://openlibrary.org/search.json"))
                .andRespond(withSuccess("{\"docs\":[]}", MediaType.APPLICATION_JSON));

        OpenLibraryResponse response = client.searchBooks(" ", null);

        assertThat(response.getDocs()).isEmpty();
        server.verify();
    }

    @Test
    void searchBooksDoesNotSendNullTitleOrBlankAuthor() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        OpenLibraryClient client = new OpenLibraryClient(restTemplate);

        server.expect(requestTo("https://openlibrary.org/search.json"))
                .andRespond(withSuccess("{\"docs\":[]}", MediaType.APPLICATION_JSON));

        OpenLibraryResponse response = client.searchBooks(null, " ");

        assertThat(response.getDocs()).isEmpty();
        server.verify();
    }
}
