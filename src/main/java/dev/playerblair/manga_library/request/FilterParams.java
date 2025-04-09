package dev.playerblair.manga_library.request;

import dev.playerblair.manga_library.model.Genre;
import dev.playerblair.manga_library.model.ProgressType;
import dev.playerblair.manga_library.model.Status;
import dev.playerblair.manga_library.model.Type;

import java.util.List;

public record FilterParams(
        String title,
        Type type,
        Status status,
        String author,
        List<Genre> genres,
        ProgressType progress
) {
}
