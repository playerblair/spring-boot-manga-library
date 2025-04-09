package dev.playerblair.manga_library.exception;

public class MangaNotFoundException extends RuntimeException {
    public MangaNotFoundException(Long malId) {
        super("Manga not found with malId: " + malId);
    }
}
