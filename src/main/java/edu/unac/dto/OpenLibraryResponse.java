package edu.unac.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OpenLibraryResponse {
    private List<Doc> docs;

    public List<Doc> getDocs() {
        return docs;
    }

    public void setDocs(List<Doc> docs) {
        this.docs = docs;
    }

    public static class Doc {
        private String key;
        private String title;

        @JsonProperty("author_name")
        private List<String> authorName;

        @JsonProperty("first_publish_year")
        private Integer firstPublishYear;

        @JsonProperty("edition_count")
        private Integer editionCount;

        @JsonProperty("cover_i")
        private Integer coverId;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<String> getAuthorName() {
            return authorName;
        }

        public void setAuthorName(List<String> authorName) {
            this.authorName = authorName;
        }

        public Integer getFirstPublishYear() {
            return firstPublishYear;
        }

        public void setFirstPublishYear(Integer firstPublishYear) {
            this.firstPublishYear = firstPublishYear;
        }

        public Integer getEditionCount() {
            return editionCount;
        }

        public void setEditionCount(Integer editionCount) {
            this.editionCount = editionCount;
        }

        public Integer getCoverId() {
            return coverId;
        }

        public void setCoverId(Integer coverId) {
            this.coverId = coverId;
        }
    }
}
