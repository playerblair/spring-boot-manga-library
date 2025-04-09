package dev.playerblair.manga_library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.playerblair.manga_library.exception.*;
import dev.playerblair.manga_library.model.*;
import dev.playerblair.manga_library.request.FilterParams;
import dev.playerblair.manga_library.response.JikanSearchResponse;
import dev.playerblair.manga_library.response.MangaResponse;
import dev.playerblair.manga_library.response.Pagination;
import dev.playerblair.manga_library.service.MangaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(MangaController.class)
public class MangaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MangaService mangaService;

    private Manga manga1;
    private Manga manga2;

    private MangaResponse mangaResponse1;
    private MangaResponse mangaResponse2;

    @BeforeEach
    public void setUp() {
        manga1 = new Manga(
                1L,
                "Test Manga 1",
                Type.MANGA,
                100,
                10,
                Status.FINISHED,
                "",
                List.of(new Author("Test Author 1", "www.example.com/people/1")),
                List.of(Genre.ROMANCE, Genre.SLICE_OF_LIFE),
                "www.example.com/manga/1",
                new UserProgress(
                        ProgressType.FINISHED,
                        100,
                        10,
                        10
                )
        );

        manga2 = new Manga(
                2L,
                "Test Manga 2",
                Type.MANGA,
                13,
                1,
                Status.PUBLISHING,
                "",
                List.of(new Author("Test Author 2", "www.example.com/people/2")),
                List.of(Genre.ACTION),
                "www.example.com/manga/2",
                new UserProgress(
                        ProgressType.READING,
                        4,
                        0,
                        6
                )
        );

        mangaResponse1 = new MangaResponse(
                1L,
                "Test Manga 1",
                Type.MANGA,
                100,
                10,
                Status.FINISHED,
                "",
                List.of(new Author("Test Author 1", "www.example.com/people/1")),
                List.of(
                        new MangaResponse.GenreWrapper("Romance"),
                        new MangaResponse.GenreWrapper("Slice of Life")
                ),
                "www.example.com/manga/1"
        );

        mangaResponse2 = new MangaResponse(
                2L,
                "Test Manga 2",
                Type.MANGA,
                13,
                1,
                Status.PUBLISHING,
                "",
                List.of(new Author("Test Author 2", "www.example.com/people/2")),
                List.of(new MangaResponse.GenreWrapper("Action")),
                "www.example.com/manga/2"
        );
    }

    @Test
    public void whenGetAllMangaIsCalled_shouldReturn200AndAllManga() throws Exception{
        // mock service behaviour
        given(mangaService.getAllManga()).willReturn(List.of(manga1, manga2));

        // execute the method under test + assertions
        mockMvc.perform(get("/api/manga"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].malId").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Manga 1"))
                .andExpect(jsonPath("$[1].malId").value(2))
                .andExpect(jsonPath("$[1].title").value("Test Manga 2"));
    }

    @Test
    public void whenGetMangaIsCalled_givenValidId_shouldReturn200AndManga() throws Exception {
        // mock service behaviour
        given(mangaService.getManga(1L)).willReturn(manga1);

        // execute the method under test + assertions
        mockMvc.perform(get("/api/manga/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.malId").value(1))
                .andExpect(jsonPath("$.title").value("Test Manga 1"));
    }

    @Test
    public void whenGetMangaIsCalled_givenInvalidId_shouldReturn404AndErrorResponse() throws Exception {
        // mock service behaviour
        given(mangaService.getManga(3L)).willThrow(new MangaNotFoundException(3L));

        // execute the method under test + assertions
        mockMvc.perform(get("/api/manga/3"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Manga not found with malId: 3"));
    }

    @Test
    public void whenSearchMangaIsCalled_shouldReturn200AndSearchResults() throws Exception {
        // setup test data
        JikanSearchResponse expectedResponse = new JikanSearchResponse(
                new Pagination(1, false),
                List.of(mangaResponse1, mangaResponse2)
        );
        String query = "Test Manga";

        // mock service behaviour
        given(mangaService.searchManga(query)).willReturn(expectedResponse);

        // execute the method under test + assertions
        mockMvc.perform(get("/api/manga/search").param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void whenSearchMangaNextIsCalled_givenSearchHasNextPage_shouldReturn200AndSearchResults() throws Exception {
        // setup test data
        JikanSearchResponse expectedResponse = new JikanSearchResponse(
                new Pagination(1, false),
                List.of(mangaResponse1, mangaResponse2)
        );

        // mock service behaviour
        given(mangaService.searchMangaNext()).willReturn(expectedResponse);

        // execute the method under test + assertions
        mockMvc.perform(get("/api/manga/search/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void whenSearchMangaNextIsCalled_givenNoInitialSearch_shouldReturn400AndErrorResponse() throws Exception {
        // mock service behaviour
        given(mangaService.searchMangaNext()).willThrow(new NoInitialSearchException());

        // execute the method under test + assertions
        mockMvc.perform(get("/api/manga/search/next"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cannot fetch next page: no initial search has been performed"));
    }

    @Test
    public void whenSearchMangaNextIsCalled_givenSearchHasNoNextPage_shouldReturn400AndErrorResponse() throws Exception {
        // mock service behaviour
        given(mangaService.searchMangaNext()).willThrow(new PaginationLimitReachedException());

        // execute the method under test + assertions
        mockMvc.perform(get("/api/manga/search/next"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cannot fetch next page: already at the last page of results."));
    }

    @Test
    public void whenAddMangaIsCalled_givenValidId_shouldReturn204AndManga() throws Exception {
        // mock service behaviour
        given(mangaService.addManga(1L)).willReturn(manga1);

        // execute the method under test + assertions
        mockMvc.perform(post("/api/manga").queryParam("malId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.malId").value(manga1.malId()))
                .andExpect(jsonPath("$.title").value(manga1.title()));
    }

    @Test
    public void whenAddMangaIsCalled_givenInvalidId_shouldReturn404AndErrorResponse() throws Exception {
        // mock service behaviour
        given(mangaService.addManga(3L)).willThrow(new MangaNotFoundInSearchResultsException(3L));

        // execute the method under test + assertions
        mockMvc.perform(post("/api/manga").queryParam("malId", "3"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Manga not found with malId: 3"));
    }

    @Test
    public void whenRefreshAllMangaIsCalled_shouldReturn200() throws Exception {
        // execute the method under test + assertions
        mockMvc.perform(patch("/api/manga/refresh-all"))
                .andExpect(status().isOk());
    }

    @Test
    public void whenRefreshMangaIsCalled_givenValidId_shouldReturn200AndUpdatedManga() throws Exception {
        // mock service behaviour
        given(mangaService.refreshManga(1L)).willReturn(manga1);

        // execute the method under test + assertions
        mockMvc.perform(patch("/api/manga/1/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.malId").value(manga1.malId()))
                .andExpect(jsonPath("$.title").value(manga1.title()));
    }

    @Test
    public void whenRefreshMangaIsCalled_givenInvalidId_shouldReturn404AndErrorResponse() throws Exception {
        // mock service behaviour
        given(mangaService.refreshManga(3L)).willThrow(new MangaNotFoundException(3L));

        // execute the method under test + assertions
        mockMvc.perform(patch("/api/manga/3/refresh"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Manga not found with malId: 3"));

    }

    @Test
    public void whenGetProgressIsCalled_givenValidId_shouldReturn200AndProgress() throws Exception {
        // setup test data
        UserProgress progress = new UserProgress(
                ProgressType.READING,
                13,
                1,
                7
        );

        // mock service behaviour
        given(mangaService.getProgress(1L)).willReturn(progress);

        // execute the method under test + assertions
        mockMvc.perform(get("/api/manga/1/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progress").value(progress.progress().getLabel()))
                .andExpect(jsonPath("$.chaptersRead").value(progress.chaptersRead()))
                .andExpect(jsonPath("$.volumesRead").value(progress.volumesRead()))
                .andExpect(jsonPath("$.rating").value(progress.rating()));
    }

    @Test
    public void whenGetProgress_givenInvalidId_shouldReturn404AndErrorResponse() throws Exception {
        // mock service behavior
        given(mangaService.getProgress(3L)).willThrow(new MangaNotFoundException(3L));

        // execute the method under test + assertions
        mockMvc.perform(get("/api/manga/3/progress"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Manga not found with malId: 3"));
    }

    @Test
    public void whenUpdateProgressIsCalled_givenValidId_shouldReturn200AndUpdatedManga() throws Exception {
        // setup test data
        UserProgress progressUpdate = new UserProgress(
                ProgressType.READING,
                13,
                1,
                7
        );
        String jsonRequest = objectMapper.writeValueAsString(progressUpdate);

        // mock service behaviour
        given(mangaService.updateProgress(1L, progressUpdate)).willReturn(manga1);

        // execute the method under test + assertions
        mockMvc.perform(patch("/api/manga/1/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.malId").value(manga1.malId()))
                .andExpect(jsonPath("$.title").value(manga1.title()));
    }

    @Test
    public void whenUpdateProgressIsCalled_givenInvalidId_shouldReturn404AndErrorResponse() throws Exception {
        // setup test data
        UserProgress progressUpdate = new UserProgress(
                ProgressType.READING,
                13,
                1,
                7
        );
        String jsonRequest = objectMapper.writeValueAsString(progressUpdate);

        // mock service behaviour
        given(mangaService.updateProgress(3L, progressUpdate)).willThrow(new MangaNotFoundException(3L));

        // execute the method under test + assertions
        mockMvc.perform(patch("/api/manga/3/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Manga not found with malId: 3"));
    }

    @Test
    public void whenUpdateProgressIsCalled_givenInvalidProgress_shouldReturn400AndErrorResponse() throws Exception {
        // setup test data
        UserProgress progressUpdate = new UserProgress(
                ProgressType.FINISHED,
                13,
                1,
                7
        );
        String jsonRequest = objectMapper.writeValueAsString(progressUpdate);
        String errorMessage = "Cannot set progress to 'Finished' as the status of 'Test Manga 2' is 'Publishing'";

        // mock service behaviour
        given(mangaService.updateProgress(2L, progressUpdate))
                .willThrow(new InvalidProgressUpdateException(errorMessage));

        // execute the method under test + assertions
        mockMvc.perform(patch("/api/manga/2/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    public void whenUpdateProgressIsCalled_givenNegativeChaptersRead_shouldReturn400AndErrorResponse() throws Exception {
        // setup test data
        UserProgress progressUpdate = new UserProgress(
                ProgressType.READING,
                -1,
                1,
                7
        );
        String jsonRequest = objectMapper.writeValueAsString(progressUpdate);
        String errorMessage = "Cannot set chaptersRead to a negative number.";

        // mock service behaviour
        given(mangaService.updateProgress(2L, progressUpdate))
                .willThrow(new InvalidProgressUpdateException(errorMessage));

        // execute the method under test + assertions
        mockMvc.perform(patch("/api/manga/2/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    public void whenUpdateProgressIsCalled_givenInvalidChaptersRead_shouldReturn400AndErrorResponse() throws Exception {
        // setup test data
        UserProgress progressUpdate = new UserProgress(
                ProgressType.READING,
                20,
                1,
                7
        );
        String jsonRequest = objectMapper.writeValueAsString(progressUpdate);
        String errorMessage = "Cannot set chaptersRead to 20 as 'Test Manga 2' only has 13 chapter(s).";

        // mock service behaviour
        given(mangaService.updateProgress(2L, progressUpdate))
                .willThrow(new InvalidProgressUpdateException(errorMessage));

        // execute the method under test + assertions
        mockMvc.perform(patch("/api/manga/2/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    public void whenUpdateProgressIsCalled_givenNegativeVolumesRead_shouldReturn400AndErrorResponse() throws Exception {
        // setup test data
        UserProgress progressUpdate = new UserProgress(
                ProgressType.READING,
                13,
                -1,
                7
        );
        String jsonRequest = objectMapper.writeValueAsString(progressUpdate);
        String errorMessage = "Cannot set volumesRead to a negative number.";

        // mock service behaviour
        given(mangaService.updateProgress(2L, progressUpdate))
                .willThrow(new InvalidProgressUpdateException(errorMessage));

        // execute the method under test + assertions
        mockMvc.perform(patch("/api/manga/2/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    public void whenUpdateProgressIsCalled_givenInvalidVolumesRead_shouldReturn400AndErrorResponse() throws Exception {
        // setup test data
        UserProgress progressUpdate = new UserProgress(
                ProgressType.READING,
                13,
                2,
                7
        );
        String jsonRequest = objectMapper.writeValueAsString(progressUpdate);
        String errorMessage = "Cannot set volumesRead to 2 as 'Test Manga 2' only has 1 volume(s).";

        // mock service behaviour
        given(mangaService.updateProgress(2L, progressUpdate))
                .willThrow(new InvalidProgressUpdateException(errorMessage));

        // execute the method under test + assertions
        mockMvc.perform(patch("/api/manga/2/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    public void whenDeleteMangaIsCalled_givenValidId_shouldReturn200AndDeletedManga() throws Exception {
        // mock service behaviour
        given(mangaService.deleteManga(1L)).willReturn(manga1);

        // execute the method under test + assertions
        mockMvc.perform(delete("/api/manga/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.malId").value(manga1.malId()))
                .andExpect(jsonPath("$.title").value(manga1.title()));
    }

    @Test
    public void whenDeleteMangaIsCalled_givenInvalidId_shouldReturn404AndErrorResponse() throws Exception {
        // mock service behaviour
        given(mangaService.deleteManga(3L)).willThrow(new MangaNotFoundException(3L));

        // execute the method under test + assertions
        mockMvc.perform(delete("/api/manga/3"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Manga not found with malId: 3"));
    }

    @Test
    public void whenFilterMangaIsCalled_givenSomeParams_shouldReturn200AndFilteredManga() throws Exception {
        // setup test data
        FilterParams filter = new FilterParams(
                "Test Manga",
                Type.MANGA,
                Status.FINISHED,
                "",
                List.of(),
                null
        );
        String jsonRequest = objectMapper.writeValueAsString(filter);

        // mock service behaviour
        given(mangaService.filterManga(filter)).willReturn(List.of(manga1));

        // execute the method under test + assertions
        mockMvc.perform(post("/api/manga/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].malId").value(manga1.malId()))
                .andExpect(jsonPath("$[0].title").value(manga1.title()));
    }

    @Test
    public void whenFilterMangaIsCalled_givenNoParams_shouldReturn200AndAllManga() throws Exception {
        // setup test data
        FilterParams filter = new FilterParams(
                "",
                null,
                null,
                "",
                List.of(),
                null
        );
        String jsonRequest = objectMapper.writeValueAsString(filter);

        // mock service behaviour
        given(mangaService.filterManga(filter)).willReturn(List.of(manga1, manga2));

        // execute the method under test + assertions
        mockMvc.perform(post("/api/manga/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].malId").value(manga1.malId()))
                .andExpect(jsonPath("$[0].title").value(manga1.title()))
                .andExpect(jsonPath("$[1].malId").value(manga2.malId()))
                .andExpect(jsonPath("$[1].title").value(manga2.title()));
    }
}
