package dev.playerblair.manga_library.repository.impl;

import dev.playerblair.manga_library.model.Manga;
import dev.playerblair.manga_library.repository.CustomMangaRepository;
import dev.playerblair.manga_library.request.FilterParams;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class CustomMangaRepositoryImpl implements CustomMangaRepository {

    private final MongoTemplate mongoTemplate;

    public CustomMangaRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Manga> findByDynamicCriteria(FilterParams filter) {
        if (filter == null) {
            return mongoTemplate.findAll(Manga.class);
        }

        Query query = new Query();

        if (filter.title() != null && !filter.title().isBlank()) {
            query.addCriteria(Criteria.where("title").regex(filter.title(), "i"));
        }

        if (filter.type() != null) {
            query.addCriteria(Criteria.where("type").is(filter.type()));
        }

        if (filter.status() != null) {
            query.addCriteria(Criteria.where("status").is(filter.status()));
        }

        if (filter.author() != null && !filter.author().isBlank()) {
            query.addCriteria(Criteria.where("author.name").regex(filter.author(), "i"));
        }

        if (filter.genres() != null && !filter.genres().isEmpty()) {
            query.addCriteria(Criteria.where("genres").all(filter.genres()));
        }

        if (filter.progress() != null) {
            query.addCriteria(Criteria.where("progress.progress").is(filter.progress()));
        }

        return mongoTemplate.find(query, Manga.class);
    }
}
