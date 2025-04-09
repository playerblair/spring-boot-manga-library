package dev.playerblair.manga_library.model;

public record UserProgress(
        ProgressType progress,
        int chaptersRead,
        int volumesRead,
        int rating
) {
}
