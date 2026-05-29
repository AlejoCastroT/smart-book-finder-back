package edu.unac.repository;

import edu.unac.model.FavoriteBook;
import edu.unac.model.SearchHistory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:repository-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RepositoryPersistenceTest {

    @Autowired
    private SearchHistoryRepository historyRepository;

    @Autowired
    private FavoriteBookRepository favoriteRepository;

    @Test
    void savesAndQueriesSearchHistory() {
        SearchHistory history = new SearchHistory();
        history.setTitle("Clean Code");
        history.setAuthor("Robert Martin");
        history.setLanguage("ingles");
        history.setPublishedAfter(2008);

        SearchHistory saved = historyRepository.saveAndFlush(history);

        assertThat(saved.getId()).isNotNull();
        assertThat(historyRepository.findAll())
                .singleElement()
                .satisfies(found -> {
                    assertThat(found.getTitle()).isEqualTo("Clean Code");
                    assertThat(found.getAuthor()).isEqualTo("Robert Martin");
                    assertThat(found.getLanguage()).isEqualTo("ingles");
                    assertThat(found.getPublishedAfter()).isEqualTo(2008);
                    assertThat(found.getSearchDate()).isNotNull();
                });
    }

    @Test
    void savesAndQueriesFavoriteBooks() {
        FavoriteBook favorite = new FavoriteBook();
        favorite.setBookKey("/works/OL1W");

        FavoriteBook saved = favoriteRepository.saveAndFlush(favorite);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAddedAt()).isNotNull();
        assertThat(favoriteRepository.existsByBookKey("/works/OL1W")).isTrue();
        assertThat(favoriteRepository.existsByBookKey("/works/missing")).isFalse();
    }
}
