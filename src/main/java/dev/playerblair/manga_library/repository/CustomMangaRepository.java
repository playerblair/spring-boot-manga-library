package dev.playerblair.manga_library.repository;

import dev.playerblair.manga_library.model.Manga;
import dev.playerblair.manga_library.request.FilterParams;

import java.util.List;

public interface CustomMangaRepository {

    List<Manga> findByDynamicCriteria(FilterParams filter);
}
