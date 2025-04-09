package dev.playerblair.manga_library.repository;

import dev.playerblair.manga_library.model.*;
import dev.playerblair.manga_library.request.FilterParams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
public class MangaRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @Autowired
    private MangaRepository mangaRepository;

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri",  mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    public void setup() {
        Manga manga1 = new Manga(
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

        Manga manga2 = new Manga(
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

        mangaRepository.saveAll(List.of(manga1, manga2));
    }

    @AfterEach
    public void tearDown() {
        mangaRepository.deleteAll();
    }

    @Test
    public void whenFindAllIsCalled_shouldReturnAllManga() {
        List<Manga> mangaList = mangaRepository.findAll();

        assertThat(mangaList).hasSize(2);
    }

    @Test
    public void whenFindByIdIsCalled_givenValidId_shouldReturnManga() {
        Optional<Manga> optionalManga = mangaRepository.findById(1L);

        assertThat(optionalManga).isPresent();
        assertThat(optionalManga.get().title()).isEqualTo("Test Manga 1");
    }

    @Test
    public void whenFindByIdIsCalled_givenInvalidId_shouldReturnEmpty() {
        Optional<Manga> optionalManga = mangaRepository.findById(4L);

        assertThat(optionalManga).isEmpty();
    }

    @Test
    public void whenSaveIsCalled_shouldAddManga() {
        Manga manga = new Manga(
                3L,
                "Test Manga 3",
                Type.ONESHOT,
                1,
                0,
                Status.FINISHED,
                "",
                List.of(new Author("Test Author 3", "www.example.com/people/3")),
                List.of(Genre.SLICE_OF_LIFE),
                "www.example.com/manga/3",
                new UserProgress(
                        ProgressType.READING,
                        1,
                        0,
                        10
                )
        );

        mangaRepository.save(manga);

        assertThat(mangaRepository.findAll()).hasSize(3);

        Optional<Manga> optionalManga = mangaRepository.findById(3L);

        assertThat(optionalManga).isPresent();
        assertThat(optionalManga.get().title()).isEqualTo("Test Manga 3");
    }

    @Test
    public void whenSaveIsCalled_givenMangaWithIdAlreadyExists_shouldOverwriteOldManga() {
        Manga manga2 = new Manga(
                2L,
                "Test Manga 2",
                Type.MANGA,
                45,
                3,
                Status.PUBLISHING,
                "",
                List.of(new Author("Test Author 2", "www.example.com")),
                List.of(Genre.ACTION),
                "www.example.com",
                new UserProgress(
                        ProgressType.READING,
                        30,
                        2,
                        7
                )
        );

        mangaRepository.save(manga2);

        assertThat(mangaRepository.findAll()).hasSize(2);

        Optional<Manga> optionalManga = mangaRepository.findById(2L);

        assertThat(optionalManga).isPresent();

        Manga updatedManga = optionalManga.get();

        assertThat(updatedManga.chapters()).isEqualTo(45);
        assertThat(updatedManga.volumes()).isEqualTo(3);
        assertThat(updatedManga.progress().chaptersRead()).isEqualTo(30);
        assertThat(updatedManga.progress().volumesRead()).isEqualTo(2);
        assertThat(updatedManga.progress().rating()).isEqualTo(7);
    }

    @Test
    public void whenDeleteIsCalled_givenValidId_shouldDeleteManga() {
        Optional<Manga> optionalManga = mangaRepository.findById(1L);

        assertThat(optionalManga).isPresent();

        mangaRepository.delete(optionalManga.get());

        assertThat(mangaRepository.findAll()).hasSize(1);
    }

    @Test
    public void whenFindByDynamicCriteriaIsCalled_givenSomeParams_shouldReturnFilteredManga() {
        FilterParams filter = new FilterParams(
                "Manga",
                Type.MANGA,
                null,
                "",
                List.of(Genre.ACTION),
                null
        );

        List<Manga> mangaList = mangaRepository.findByDynamicCriteria(filter);

        assertThat(mangaList).hasSize(1);
        assertThat(mangaList.getFirst().title()).isEqualTo("Test Manga 2");
    }

    @Test
    public void whenFindByDynamicCriteriaIsCalled_givenNoParams_shouldReturnAllManga() {
        FilterParams filter = new FilterParams(
                "",
                null,
                null,
                "",
                List.of(),
                null
        );

        List<Manga> mangaList = mangaRepository.findByDynamicCriteria(filter);

        assertThat(mangaList).hasSize(2);
    }
}
