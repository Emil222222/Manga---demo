package az.manga.demo.repository.manga;

import az.manga.demo.enums.MangaStatus;
import az.manga.demo.model.manga.Genre;
import az.manga.demo.model.manga.Manga;
import az.manga.demo.model.manga.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface MangaRepository extends JpaRepository<Manga, Long> {

    Page<Manga> findByStatus(MangaStatus status, Pageable pageable);
    Page<Manga> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Manga> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Manga m JOIN m.genres g WHERE g IN :genres")
    Page<Manga> findByGenresIn(@Param("genres") Set<Genre> genres, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Manga m JOIN m.tags t WHERE t IN :tags")
    Page<Manga> findByTagsIn(@Param("tags") Set<Tag> tags, Pageable pageable);

    @Override
    Page<Manga> findAll(Pageable pageable);
}
