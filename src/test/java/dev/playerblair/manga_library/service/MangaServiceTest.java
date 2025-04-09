package dev.playerblair.manga_library.service;

import dev.playerblair.manga_library.client.JikanClient;
import dev.playerblair.manga_library.exception.*;
import dev.playerblair.manga_library.model.*;
import dev.playerblair.manga_library.repository.MangaRepository;
import dev.playerblair.manga_library.request.FilterParams;
import dev.playerblair.manga_library.response.JikanResponse;
import dev.playerblair.manga_library.response.JikanSearchResponse;
import dev.playerblair.manga_library.response.MangaResponse;
import dev.playerblair.manga_library.response.Pagination;
import dev.playerblair.manga_library.util.MangaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MangaServiceTest {

    @Mock
    private MangaRepository mangaRepository;

    @Mock
    private JikanClient jikanClient;

    @InjectMocks
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
    public void whenGetAllMangaIsCalled_shouldReturnAllManga() {
        // mock repository behaviour
        given(mangaRepository.findAll()).willReturn(List.of(manga1, manga2));

        // assertion
        assertThat(mangaService.getAllManga()).hasSize(2);
    }

    @Test
    public void whenGetMangaIsCalled_givenValidId_shouldReturnManga() {
        // mock repository behaviour
        given(mangaRepository.findById(manga1.malId())).willReturn(Optional.of(manga1));

        // execute the method under test
        Manga manga = mangaService.getManga(manga1.malId());

        // assertions
        assertThat(manga.malId()).isEqualTo(manga1.malId());
        assertThat(manga.title()).isEqualTo(manga1.title());
    }

    @Test
    public void whenGetMangaIsCalled_givenInvalidId_shouldThrowException() {
        // mock repository behaviour
        given(mangaRepository.findById(3L)).willReturn(Optional.empty());

        // assert MangaNotFoundException thrown
        assertThatThrownBy(() -> mangaService.getManga(3L))
                .isInstanceOf(MangaNotFoundException.class);
    }

    @Test
    public void whenSearchMangaIsCalled_shouldReturnSearchResponse() {
        // setup test data
        JikanSearchResponse expectedResponse = new JikanSearchResponse(
                new Pagination(1, false),
                List.of(mangaResponse1, mangaResponse2)
        );
        String query = "Test Manga";

        // mock external api behaviour
        given(jikanClient.searchManga(query)).willReturn(expectedResponse);

        // execute method under test
        JikanSearchResponse searchResponse = mangaService.searchManga(query);

        // verify interactions + assertions
        verify(jikanClient).searchManga(query);
        assertThat(searchResponse.data()).hasSize(2);
    }

    @Test
    public void whenSearchMangaNextIsCalled_givenSearchHasNextPage_shouldReturnSearchResponse() {
        // setup test data
        JikanSearchResponse firstResponse = new JikanSearchResponse(
                new Pagination(1, true),
                List.of(mangaResponse1, mangaResponse2)
        );
        JikanSearchResponse secondResponse = new JikanSearchResponse(
                new Pagination(2, false),
                List.of(mangaResponse1, mangaResponse2)
        );
        String query = "Test Manga";

        // mock external api behaviour
        given(jikanClient.searchManga(query)).willReturn(firstResponse);
        given(jikanClient.searchManga(query, 2)).willReturn(secondResponse);

        // perform initial search
        mangaService.searchManga(query);

        // execute the method under test
        JikanSearchResponse searchResults = mangaService.searchMangaNext();

        // verify interactions + assertions
        verify(jikanClient).searchManga(query, 2);
        assertThat(searchResults.data()).hasSize(2);
    }

    @Test
    public void whenSearchMangaNextIsCalled_givenNoInitialSearch_shouldThrowException() {
        // assert NoInitialSearchException thrown
        assertThatThrownBy(() -> mangaService.searchMangaNext())
                .isInstanceOf(NoInitialSearchException.class);
    }

    @Test
    public void whenSearchMangaNextIsCalled_givenSearchHasNoNextPage_shouldThrowException() {
        // setup test data
        JikanSearchResponse firstResponse = new JikanSearchResponse(
                new Pagination(1, false),
                List.of(mangaResponse1, mangaResponse2)
        );
        String query = "Test Manga";

        // mock external api behaviour
        given(jikanClient.searchManga(query)).willReturn(firstResponse);

        // execute the method under test
        mangaService.searchManga(query);

        // assert PaginationLimitReachedException thrown
        assertThatThrownBy(() -> mangaService.searchMangaNext())
                .isInstanceOf(PaginationLimitReachedException.class);
    }

    @Test
    public void whenAddMangaIsCalled_givenValidIdAndNewManga_shouldAddAndReturnManga() {
        // setup test data
        JikanSearchResponse response = new JikanSearchResponse(
                new Pagination(1, false),
                List.of(mangaResponse1, mangaResponse2)
        );
        String query = "Test Manga";
        Manga createdManga = MangaMapper.toManga(mangaResponse1);

        // mock external api behaviour
        given(jikanClient.searchManga(query)).willReturn(response);

        // perform initial search
        mangaService.searchManga(query);

        // mock repository behaviour
        given(mangaRepository.findById(mangaResponse1.malId())).willReturn(Optional.empty());
        given(mangaRepository.save(createdManga)).willReturn(createdManga);

        // execute the method under test
        Manga manga = mangaService.addManga(mangaResponse1.malId());

        // verify interactions + assertions
        verify(mangaRepository).save(createdManga);
        assertThat(manga.malId()).isEqualTo(mangaResponse1.malId());
    }

    @Test
    public void whenAddMangaIsCalled_givenValidIdAndExistingManga_shouldUpdateAndReturnManga() {
        // setup test data
        mangaResponse2 = new MangaResponse(
                2L,
                "Test Manga 2",
                Type.MANGA,
                40,
                2,
                Status.PUBLISHING,
                "",
                List.of(new Author("Test Author 2", "www.example.com/people/2")),
                List.of(new MangaResponse.GenreWrapper("Action")),
                "www.example.com/manga/2"
        );
        JikanSearchResponse response = new JikanSearchResponse(
                new Pagination(1, false),
                List.of(mangaResponse1, mangaResponse2)
        );
        String query = "Test Manga";
        Manga updatedManga = MangaMapper.updateManga(mangaResponse2, manga2);

        // mock external api behaviour
        given(jikanClient.searchManga(query)).willReturn(response);

        // perform initial search
        mangaService.searchManga(query);

        // mock repository behaviour
        given(mangaRepository.findById(mangaResponse2.malId())).willReturn(Optional.of(manga2));
        given(mangaRepository.save(updatedManga)).willReturn(updatedManga);

        // execute the method under test
        Manga manga = mangaService.addManga(mangaResponse2.malId());

        // verify interactions + assertions
        verify(mangaRepository).save(updatedManga);
        assertThat(manga.malId()).isEqualTo(mangaResponse2.malId());
        assertThat(manga.chapters()).isEqualTo(40);
        assertThat(manga.volumes()).isEqualTo(2);

    }

    @Test
    public void whenAddMangaIsCalled_givenInvalidId_shouldThrowException() {
        // setup test data
        JikanSearchResponse response = new JikanSearchResponse(
                new Pagination(1, false),
                List.of(mangaResponse1, mangaResponse2)
        );
        String query = "Test Manga";

        // mock external api behaviour
        given(jikanClient.searchManga(query)).willReturn(response);

        // perform initial search
        mangaService.searchManga(query);

        // assert MangaNotFoundInSearchException thrown
        assertThatThrownBy(() -> mangaService.addManga(3L))
                .isInstanceOf(MangaNotFoundInSearchResultsException.class);
    }

    @Test
    public void whenRefreshAllMangaIsCalled_shouldUpdateAllManga() {
        // setup test data
        mangaResponse2 = new MangaResponse(
                2L,
                "Test Manga 2",
                Type.MANGA,
                40,
                2,
                Status.PUBLISHING,
                "",
                List.of(new Author("Test Author 2", "www.example.com/people/2")),
                List.of(
                        new MangaResponse.GenreWrapper("Romance"),
                        new MangaResponse.GenreWrapper("Slice of Life")
                ),
                "www.example.com/manga/2"
        );
        Manga updatedManga2 = MangaMapper.updateManga(mangaResponse2, manga2);

        // mock external api behaviour
        given(jikanClient.getManga(manga1.malId())).willReturn(new JikanResponse(mangaResponse1));
        given(jikanClient.getManga(manga2.malId())).willReturn(new JikanResponse(mangaResponse2));

        // mock repository behaviour
        given(mangaRepository.findAll()).willReturn(List.of(manga1, manga2));

        // execute the method under test
        mangaService.refreshAllManga();

        // verify expected interactions with repository;
        verify(mangaRepository).save(manga1);
        verify(mangaRepository).save(updatedManga2);
    }

    @Test
    public void whenRefreshMangaIsCalled_givenValidId_shouldUpdateAndReturnManga() {
        // setup test data
        mangaResponse2 = new MangaResponse(
                2L,
                "Test Manga 2",
                Type.MANGA,
                40,
                2,
                Status.PUBLISHING,
                "",
                List.of(new Author("Test Author 2", "www.example.com/people/2")),
                List.of(
                        new MangaResponse.GenreWrapper("Romance"),
                        new MangaResponse.GenreWrapper("Slice of Life")
                ),
                "www.example.com/manga/2"
        );
        Manga updatedManga = MangaMapper.updateManga(mangaResponse2, manga2);

        // mock external api behaviour
        given(jikanClient.getManga(manga2.malId())).willReturn(new JikanResponse(mangaResponse2));

        // mock repository behaviour
        given(mangaRepository.findById(manga2.malId())).willReturn(Optional.of(manga2));
        given(mangaRepository.save(updatedManga)).willReturn(updatedManga);

        // execute the method under test
        Manga manga = mangaService.refreshManga(manga2.malId());

        // verify interactions + assertions
        verify(mangaRepository).save(updatedManga);
        assertThat(manga.malId()).isEqualTo(manga2.malId());
        assertThat(manga.chapters()).isEqualTo(mangaResponse2.chapters());
        assertThat(manga.volumes()).isEqualTo(mangaResponse2.volumes());
    }

   @Test
   public void whenRefreshMangaIsCalled_givenInvalidId_shouldThrowException() {
       // mock repository behaviour
       given(mangaRepository.findById(3L)).willReturn(Optional.empty());

       // assert MangaNotFoundException thrown
       assertThatThrownBy(() -> mangaService.refreshManga(3L))
               .isInstanceOf(MangaNotFoundException.class);
   }

   @Test
   public void whenGetProgressIsCalled_givenValidId_shouldReturnProgress() {
       // mock repository behaviour
       given(mangaRepository.findById(manga1.malId())).willReturn(Optional.of(manga1));

       // execute the method under test
       UserProgress progress = mangaService.getProgress(manga1.malId());

       // assertions
       assertThat(progress.progress()).isEqualTo(manga1.progress().progress());
       assertThat(progress.chaptersRead()).isEqualTo(manga1.progress().chaptersRead());
       assertThat(progress.volumesRead()).isEqualTo(manga1.progress().volumesRead());
       assertThat(progress.rating()).isEqualTo(manga1.progress().rating());
   }

   @Test
   public void whenGetProgressIsCalled_givenInvalidId_shouldThrowException() {
       // mock repository behaviour
       given(mangaRepository.findById(3L)).willReturn(Optional.empty());

       // assert MangaNotFoundException thrown
       assertThatThrownBy(() -> mangaService.getProgress(3L))
               .isInstanceOf(MangaNotFoundException.class);
   }

   @Test
   public void whenUpdateProgressIsCalled_givenValidId_shouldUpdateAndReturnManga() {
       // setup test data
       UserProgress progressUpdate = new UserProgress(
               ProgressType.READING,
               13,
               1,
               7
       );
       Manga updatedManga = MangaMapper.updateProgress(progressUpdate, manga2);

       // mock repository behaviour
       given(mangaRepository.findById(manga2.malId())).willReturn(Optional.of(manga2));
       given(mangaRepository.save(updatedManga)).willReturn(updatedManga);

       // execute the method under test
       Manga manga = mangaService.updateProgress(manga2.malId(), progressUpdate);

       // assertions
       assertThat(manga.malId()).isEqualTo(manga2.malId());
       assertThat(manga.progress().progress()).isEqualTo(progressUpdate.progress());
       assertThat(manga.progress().chaptersRead()).isEqualTo(progressUpdate.chaptersRead());
       assertThat(manga.progress().volumesRead()).isEqualTo(progressUpdate.volumesRead());
       assertThat(manga.progress().rating()).isEqualTo(progressUpdate.rating());
   }

    @Test
    public void whenUpdateProgressIsCalled_givenInvalidId_shouldThrowException() {
        // setup test data
        UserProgress progressUpdate = new UserProgress(
                ProgressType.READING,
                13,
                1,
                7
        );

        // mock repository behaviour
        given(mangaRepository.findById(3L)).willReturn(Optional.empty());

        // assert MangaNotFoundException thrown
        assertThatThrownBy(() -> mangaService.updateProgress(3L, progressUpdate))
                .isInstanceOf(MangaNotFoundException.class);
    }

    @Test
    public void whenUpdateProgressIsCalled_givenInvalidProgress_shouldThrowException() {
        // setup test data
        UserProgress progressUpdate = new UserProgress(
                ProgressType.FINISHED,
                13,
                1,
                7
        );

        // mock repository behaviour
        given(mangaRepository.findById(2L)).willReturn(Optional.of(manga2));

        // assert MangaNotFoundException thrown
        assertThatThrownBy(() -> mangaService.updateProgress(2L, progressUpdate))
                .isInstanceOf(InvalidProgressUpdateException.class)
                .hasMessage("Cannot set progress to 'Finished' as the status of 'Test Manga 2' is 'Publishing'");
    }

    @Test
    public void whenUpdateProgressIsCalled_givenNegativeChaptersRead_shouldThrowException() {
        // setup test data
        UserProgress progressUpdate = new UserProgress(
                ProgressType.READING,
                -1,
                1,
                7
        );

        // mock repository behaviour
        given(mangaRepository.findById(2L)).willReturn(Optional.of(manga2));

        // assert MangaNotFoundException thrown
        assertThatThrownBy(() -> mangaService.updateProgress(2L, progressUpdate))
                .isInstanceOf(InvalidProgressUpdateException.class)
                .hasMessage("Cannot set chaptersRead to a negative number.");
    }

    @Test
    public void whenUpdateProgressIsCalled_givenInvalidChaptersRead_shouldThrowException() {
        // setup test data
        UserProgress progressUpdate = new UserProgress(
                ProgressType.READING,
                20,
                1,
                7
        );

        // mock repository behaviour
        given(mangaRepository.findById(2L)).willReturn(Optional.of(manga2));

        // assert MangaNotFoundException thrown
        assertThatThrownBy(() -> mangaService.updateProgress(2L, progressUpdate))
                .isInstanceOf(InvalidProgressUpdateException.class)
                .hasMessage("Cannot set chaptersRead to 20 as 'Test Manga 2' only has 13 chapter(s).");
    }

    @Test
    public void whenUpdateProgressIsCalled_givenNegativeVolumesRead_shouldThrowException() {
        // setup test data
        UserProgress progressUpdate = new UserProgress(
                ProgressType.READING,
                13,
                -1,
                7
        );

        // mock repository behaviour
        given(mangaRepository.findById(2L)).willReturn(Optional.of(manga2));

        // assert MangaNotFoundException thrown
        assertThatThrownBy(() -> mangaService.updateProgress(2L, progressUpdate))
                .isInstanceOf(InvalidProgressUpdateException.class)
                .hasMessage("Cannot set volumesRead to a negative number.");
    }

    @Test
    public void whenUpdateProgressIsCalled_givenInvalidVolumesRead_shouldThrowException() {
        // setup test data
        UserProgress progressUpdate = new UserProgress(
                ProgressType.READING,
                13,
                2,
                7
        );

        // mock repository behaviour
        given(mangaRepository.findById(2L)).willReturn(Optional.of(manga2));

        // assert MangaNotFoundException thrown
        assertThatThrownBy(() -> mangaService.updateProgress(2L, progressUpdate))
                .isInstanceOf(InvalidProgressUpdateException.class)
                .hasMessage("Cannot set volumesRead to 2 as 'Test Manga 2' only has 1 volume(s).");
    }

    @Test
    public void whenDeleteMangaIsCalled_givenValidId_shouldDeleteAndReturnManga() {
        // mock repository behaviour
        given(mangaRepository.findById(manga1.malId())).willReturn(Optional.of(manga1));

        // execute the method under test
        Manga manga = mangaService.deleteManga(manga1.malId());

        // verify interactions + assertions
        verify(mangaRepository).delete(manga1);
        assertThat(manga.malId()).isEqualTo(manga1.malId());
    }

    @Test
    public void whenDeleteMangaIsCalled_givenInvalidId_shouldThrowException() {
        // mock repository behaviour
        given(mangaRepository.findById(3L)).willReturn(Optional.empty());

        // assert MangaNotFoundException thrown
        assertThatThrownBy(() -> mangaService.deleteManga(3L))
                .isInstanceOf(MangaNotFoundException.class);
    }

    @Test
    public void whenFilterMangaIsCalled_givenSomeParams_shouldReturnFilteredManga() {
        // setup test data
        FilterParams filter = new FilterParams(
                "Test Manga",
                Type.MANGA,
                Status.FINISHED,
                "",
                List.of(),
                null
        );

        // mock repository behaviour
        given(mangaRepository.findByDynamicCriteria(filter)).willReturn(List.of(manga1));

        // execute the method under test
        List<Manga> manga = mangaService.filterManga(filter);

        // assertions
        assertThat(manga).hasSize(1);
        assertThat(manga.getFirst().malId()).isEqualTo(manga1.malId());
        assertThat(manga.getFirst().title()).isEqualTo(manga1.title());
    }

    @Test
    public void whenFilterMangaIsCalled_givenNoParams_shouldReturnAllManga() {
        // setup test data
        FilterParams filter = new FilterParams(
                "",
                null,
                null,
                "",
                List.of(),
                null
        );

        // mock repository behaviour
        given(mangaRepository.findByDynamicCriteria(filter)).willReturn(List.of(manga1, manga2));

        // execute the method under test + assertions
        assertThat(mangaService.filterManga(filter)).hasSize(2);
    }
}
