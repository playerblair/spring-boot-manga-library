package dev.playerblair.manga_library.controller;

import dev.playerblair.manga_library.model.Manga;
import dev.playerblair.manga_library.model.UserProgress;
import dev.playerblair.manga_library.request.FilterParams;
import dev.playerblair.manga_library.response.JikanSearchResponse;
import dev.playerblair.manga_library.service.MangaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manga")
public class MangaController {

    private final MangaService mangaService;

    public MangaController(MangaService mangaService) {
        this.mangaService = mangaService;
    }

    @GetMapping
    public ResponseEntity<List<Manga>> getAllManga() {
        return ResponseEntity.ok(mangaService.getAllManga());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Manga> getManga(@PathVariable("id") Long malId) {
        return ResponseEntity.ok(mangaService.getManga(malId));
    }

    @GetMapping("/search")
    public ResponseEntity<JikanSearchResponse> searchManga(@RequestParam String query) {
        return ResponseEntity.ok(mangaService.searchManga(query));
    }

    @GetMapping("/search/next")
    public ResponseEntity<JikanSearchResponse> searchMangaNext() {
        return ResponseEntity.ok(mangaService.searchMangaNext());
    }

    @PostMapping
    public ResponseEntity<Manga> addManga(@RequestParam Long malId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mangaService.addManga(malId));
    }

    @PatchMapping("/refresh-all")
    @ResponseStatus(HttpStatus.OK)
    public void refreshAllManga() {
        mangaService.refreshAllManga();
    }

    @PatchMapping("/{id}/refresh")
    public ResponseEntity<Manga> refreshManga(@PathVariable("id") Long malId) {
        return ResponseEntity.ok(mangaService.refreshManga(malId));
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<UserProgress> getProgress(@PathVariable("id") Long malId) {
        return ResponseEntity.ok(mangaService.getProgress(malId));
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<Manga> updateProgress(@PathVariable("id") Long malId, @RequestBody UserProgress progressUpdate) {
        return ResponseEntity.ok(mangaService.updateProgress(malId, progressUpdate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Manga> deleteManga(@PathVariable("id") Long malId) {
        return ResponseEntity.ok(mangaService.deleteManga(malId));
    }

    @PostMapping("/filter")
    public ResponseEntity<List<Manga>> filterManga(@RequestBody FilterParams filter) {
        return ResponseEntity.ok(mangaService.filterManga(filter));
    }
}
