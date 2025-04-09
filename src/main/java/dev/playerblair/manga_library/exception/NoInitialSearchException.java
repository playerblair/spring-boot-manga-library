package dev.playerblair.manga_library.exception;

public class NoInitialSearchException extends RuntimeException {
    public NoInitialSearchException() {
        super("Cannot fetch next page: no initial search has been performed");
    }
}
