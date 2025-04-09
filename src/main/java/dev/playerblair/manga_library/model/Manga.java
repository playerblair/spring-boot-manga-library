package dev.playerblair.manga_library.model;

import org.springframework.data.annotation.Id;

import java.util.List;

public record Manga(
        @Id Long malId,
        String title,
        Type type,
        int chapters,
        int volumes,
        Status status,
        String synopsis,
        List<Author> authors,
        List<Genre> genres,
        String url,
        UserProgress progress
) {
}
