package dev.playerblair.manga_library.service;

import dev.playerblair.manga_library.client.JikanClient;
import dev.playerblair.manga_library.exception.*;
import dev.playerblair.manga_library.model.Manga;
import dev.playerblair.manga_library.model.ProgressType;
import dev.playerblair.manga_library.model.Status;
import dev.playerblair.manga_library.model.UserProgress;
import dev.playerblair.manga_library.repository.MangaRepository;
import dev.playerblair.manga_library.request.FilterParams;
import dev.playerblair.manga_library.response.JikanSearchResponse;
import dev.playerblair.manga_library.response.MangaResponse;
import dev.playerblair.manga_library.response.Pagination;
import dev.playerblair.manga_library.util.MangaMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MangaService {

    private final MangaRepository mangaRepository;
    private final JikanClient jikanClient;

    private String lastSearchQuery;
    private Pagination lastSearchPagination;
    private Map<Long, MangaResponse> lastSearchResults;

    public MangaService(MangaRepository mangaRepository, JikanClient jikanClient) {
        this.mangaRepository = mangaRepository;
        this.jikanClient = jikanClient;
        this.lastSearchResults = new HashMap<>();
    }

    public List<Manga> getAllManga() {
        return mangaRepository.findAll();
    }

    public Manga getManga(Long malId) {
        return mangaRepository.findById(malId)
                .orElseThrow(() -> new MangaNotFoundException(malId));
    }

    public JikanSearchResponse searchManga(String query) {
        return processSearchResponse(jikanClient.searchManga(query), query);
    }

    public JikanSearchResponse searchManga(String query, int page) {
        return processSearchResponse(jikanClient.searchManga(query, page), query);
    }

    public JikanSearchResponse searchMangaNext() {
        if (lastSearchQuery == null) {
            throw new NoInitialSearchException();
        }

        if (!lastSearchPagination.hasNext()) {
            throw new PaginationLimitReachedException();
        }

        return searchManga(lastSearchQuery, lastSearchPagination.currentPage() + 1);
    }

    public Manga addManga(Long malId) {
        MangaResponse mangaResponse = lastSearchResults.get(malId);
        if (mangaResponse == null) {
            throw new MangaNotFoundInSearchResultsException(malId);
        }

        return mangaRepository.findById(malId)
                .map(manga -> mangaRepository.save(MangaMapper.updateManga(mangaResponse, manga)))
                .orElseGet(() -> mangaRepository.save(MangaMapper.toManga(mangaResponse)));
    }

    public void refreshAllManga() {
        mangaRepository.findAll()
                .forEach(manga -> {
                    MangaResponse mangaResponse = jikanClient.getManga(manga.malId()).data();
                    mangaRepository.save(MangaMapper.updateManga(mangaResponse, manga));
                });
    }

    public Manga refreshManga(Long malId) {
        return mangaRepository.findById(malId)
                .map(manga -> {
                    MangaResponse mangaResponse = jikanClient.getManga(manga.malId()).data();
                    return mangaRepository.save(MangaMapper.updateManga(mangaResponse, manga));
                })
                .orElseThrow(() -> new MangaNotFoundException(malId));
    }

    public UserProgress getProgress(Long malId) {
        return mangaRepository.findById(malId)
                .map(Manga::progress)
                .orElseThrow(() -> new MangaNotFoundException(malId));
    }

    public Manga updateProgress(Long malId, UserProgress progressUpdate) {
        return mangaRepository.findById(malId)
                .map(manga -> mangaRepository.save(MangaMapper.updateProgress(validateProgressUpdate(progressUpdate, manga), manga)))
                .orElseThrow(() -> new MangaNotFoundException(malId));
    }

    public Manga deleteManga(Long malId) {
        return mangaRepository.findById(malId)
                .map(manga -> {
                    mangaRepository.delete(manga);
                    return manga;
                })
                .orElseThrow(() -> new MangaNotFoundException(malId));
    }

    public List<Manga> filterManga(FilterParams filter) {
        return mangaRepository.findByDynamicCriteria(filter);
    }

    private JikanSearchResponse processSearchResponse(JikanSearchResponse searchResponse, String query) {
        lastSearchQuery = query;
        lastSearchPagination = searchResponse.pagination();
        lastSearchResults.clear();
        searchResponse.data().forEach(manga -> lastSearchResults.put(manga.malId(), manga));

        return searchResponse;
    }

    private UserProgress validateProgressUpdate(UserProgress progressUpdate, Manga manga) {
        boolean isOngoing = manga.status() != Status.FINISHED && manga.status() != Status.DISCONTINUED;
        if (progressUpdate.progress() == ProgressType.FINISHED && isOngoing) {
            String message = String.format(
                    "Cannot set progress to 'Finished' as the status of '%s' is '%s'",
                    manga.title(),
                    manga.status().getLabel()
            );
            throw new InvalidProgressUpdateException(message);
        }

        if (progressUpdate.progress() == ProgressType.FINISHED) {
            return new UserProgress(
                    ProgressType.FINISHED,
                    manga.chapters(),
                    manga.volumes(),
                    progressUpdate.rating()
            );
        }

        if (progressUpdate.chaptersRead() < 0) {
            throw new InvalidProgressUpdateException("Cannot set chaptersRead to a negative number.");
        }

        if (progressUpdate.chaptersRead() > manga.chapters()) {
            String message = String.format(
                    "Cannot set chaptersRead to %d as '%s' only has %d chapter(s).",
                    progressUpdate.chaptersRead(),
                    manga.title(),
                    manga.chapters()
            );
            throw new InvalidProgressUpdateException(message);
        }

        if (progressUpdate.volumesRead() < 0) {
            throw new InvalidProgressUpdateException("Cannot set volumesRead to a negative number.");
        }

        if (progressUpdate.volumesRead() > manga.volumes()) {
            String message = String.format(
                    "Cannot set volumesRead to %d as '%s' only has %d volume(s).",
                    progressUpdate.volumesRead(),
                    manga.title(),
                    manga.volumes()
            );
            throw new InvalidProgressUpdateException(message);
        }

        return progressUpdate;
    }
}
