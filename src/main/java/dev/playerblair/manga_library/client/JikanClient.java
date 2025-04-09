package dev.playerblair.manga_library.client;

import dev.playerblair.manga_library.model.Manga;
import dev.playerblair.manga_library.response.JikanResponse;
import dev.playerblair.manga_library.response.JikanSearchResponse;
import dev.playerblair.manga_library.response.MangaResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange("/v4/manga")
public interface JikanClient {

    @GetExchange
    JikanSearchResponse searchManga(@RequestParam("q") String query);

    @GetExchange
    JikanSearchResponse searchManga(@RequestParam("q") String query, @RequestParam(value = "page", required = false) int page);

    @GetExchange("/{malId}")
    JikanResponse getManga(@PathVariable Long malId);
}
