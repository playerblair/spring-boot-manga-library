package dev.playerblair.manga_library.util;

import dev.playerblair.manga_library.exception.InvalidProgressUpdateException;
import dev.playerblair.manga_library.model.*;
import dev.playerblair.manga_library.response.MangaResponse;

public class MangaMapper {

    public static Manga toManga(MangaResponse mangaResponse) {
        return new Manga(
                mangaResponse.malId(),
                mangaResponse.title(),
                mangaResponse.type(),
                mangaResponse.chapters(),
                mangaResponse.volumes(),
                mangaResponse.status(),
                mangaResponse.synopsis(),
                mangaResponse.authors(),
                mangaResponse.genres().stream()
                        .map(genreWrapper -> Genre.fromLabel(genreWrapper.name()))
                        .toList(),
                mangaResponse.url(),
                new UserProgress(
                        ProgressType.PLANNING,
                        0,
                        0,
                        0
                )
        );
    }

    public static Manga updateManga(MangaResponse mangaResponse, Manga manga) {
        if (!mangaResponse.malId().equals(manga.malId())) {
            throw new IllegalArgumentException("Cannot update manga with mismatched information.");
        }
        return new Manga(
                mangaResponse.malId(),
                mangaResponse.title(),
                mangaResponse.type(),
                mangaResponse.chapters(),
                mangaResponse.volumes(),
                mangaResponse.status(),
                mangaResponse.synopsis(),
                mangaResponse.authors(),
                mangaResponse.genres().stream()
                        .map(genreWrapper -> Genre.fromLabel(genreWrapper.name()))
                        .toList(),
                mangaResponse.url(),
                manga.progress()
        );
    }

    public static Manga updateProgress(UserProgress progressUpdate, Manga manga) {
        return new Manga(
                manga.malId(),
                manga.title(),
                manga.type(),
                manga.chapters(),
                manga.volumes(),
                manga.status(),
                manga.synopsis(),
                manga.authors(),
                manga.genres(),
                manga.url(),
                progressUpdate
        );
    }
}
