package dev.playerblair.manga_library.exception;

public class PaginationLimitReachedException extends RuntimeException {
    public PaginationLimitReachedException() {
        super("Cannot fetch next page: already at the last page of results.");
    }
}
