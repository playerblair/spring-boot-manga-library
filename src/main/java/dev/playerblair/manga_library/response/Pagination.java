package dev.playerblair.manga_library.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Pagination(
        @JsonProperty("current_page") int currentPage,
        @JsonProperty("has_next") boolean hasNext
) {
}
