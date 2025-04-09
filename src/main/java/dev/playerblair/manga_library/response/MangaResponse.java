package dev.playerblair.manga_library.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.playerblair.manga_library.model.Author;
import dev.playerblair.manga_library.model.Status;
import dev.playerblair.manga_library.model.Type;

import java.util.List;

public record MangaResponse(
        @JsonProperty("mal_id") Long malId,
        String title,
        Type type,
        int chapters,
        int volumes,
        Status status,
        String synopsis,
        List<Author> authors,
        List<GenreWrapper> genres,
        String url
) {

    public record GenreWrapper(String name) {}
}
