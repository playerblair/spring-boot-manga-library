package dev.playerblair.manga_library.handler;

import dev.playerblair.manga_library.exception.InvalidProgressUpdateException;
import dev.playerblair.manga_library.exception.MangaNotFoundException;
import dev.playerblair.manga_library.exception.NoInitialSearchException;
import dev.playerblair.manga_library.exception.PaginationLimitReachedException;
import dev.playerblair.manga_library.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class MangaExceptionHandler {

    @ExceptionHandler(MangaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMangaNotFoundException(MangaNotFoundException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(NoInitialSearchException.class)
    public ResponseEntity<ErrorResponse> handleNoInitialSearchException(NoInitialSearchException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(PaginationLimitReachedException.class)
    public ResponseEntity<ErrorResponse> handlePaginationLimitReachedException(PaginationLimitReachedException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidProgressUpdateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidProgressUpdateException(InvalidProgressUpdateException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
