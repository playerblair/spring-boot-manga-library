package dev.playerblair.manga_library.response;

import java.util.List;

public record JikanSearchResponse(
        Pagination pagination,
        List<MangaResponse> data
) {
}
