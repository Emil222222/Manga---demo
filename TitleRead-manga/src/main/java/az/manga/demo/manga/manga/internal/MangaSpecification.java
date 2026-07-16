package az.manga.demo.manga.manga.internal;


import az.manga.demo.common.enums.MangaStatus;
import az.manga.demo.common.enums.MangaType;

import az.manga.demo.manga.catalog.internal.Tag;
import az.manga.demo.manga.catalog.internal.Genre;
import az.manga.demo.manga.manga.internal.dto.MangaFilterDto;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MangaSpecification {

    public static Specification<Manga> filter(MangaFilterDto filter) {
        return (root, query, cb) ->{
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            boolean needDistinct = false;

            if (filter.getTitle() != null && !filter.getTitle().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")),
                        '%' + filter.getTitle().toLowerCase() + '%'));
            }

            if (filter.getAuthor() != null && !filter.getAuthor().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("author")),
                        '%' + filter.getAuthor().toLowerCase() + '%'));
            }

            if (filter.getType() != null && !filter.getType().isBlank()) {
                predicates.add(cb.equal(root.get("type"),
                        MangaType.valueOf(filter.getType())));
            }

            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                predicates.add(cb.equal(root.get("status"),
                        MangaStatus.valueOf(filter.getStatus())));
            }

            if (filter.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"),
                        filter.getMinRating()));
            }

            if (filter.getMaxRating() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("averageRating"),
                        filter.getMaxRating()));
            }

            if (filter.getGenreIds() != null && !filter.getGenreIds().isEmpty()) {
                Join<Manga, Genre> genreJoin = root.join("genre", JoinType.INNER);
                predicates.add(genreJoin.get("id").in(filter.getGenreIds()));
                needDistinct = true;
            }

            if (filter.getTagIds() != null && !filter.getTagIds().isEmpty()) {
                Join<Manga, Tag> tagJoin = root.join("tag", JoinType.INNER);
                predicates.add(tagJoin.get("id").in(filter.getTagIds()));
                needDistinct = true;
            }

            if (needDistinct && query != null) {
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
