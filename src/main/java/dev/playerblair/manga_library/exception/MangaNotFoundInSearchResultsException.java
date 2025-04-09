package dev.playerblair.manga_library.exception;

public class MangaNotFoundInSearchResultsException extends MangaNotFoundException {
    public MangaNotFoundInSearchResultsException(Long malId) {
        super(malId);
    }
}
