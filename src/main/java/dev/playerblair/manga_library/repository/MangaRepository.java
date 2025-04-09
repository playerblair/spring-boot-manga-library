package dev.playerblair.manga_library.repository;

import dev.playerblair.manga_library.model.Manga;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MangaRepository extends ListCrudRepository<Manga, Long>, CustomMangaRepository {
}
