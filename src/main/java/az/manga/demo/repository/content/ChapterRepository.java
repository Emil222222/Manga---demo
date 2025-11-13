package az.manga.demo.repository.content;

import az.manga.demo.model.content.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    // Главы конкретной манги - обычно их не так много, List ок
    List<Chapter> findByMangaIdOrderByChapterNumberAsc(Long mangaId);

    Optional<Chapter> findByMangaIdAndChapterNumber(Long mangaId, Double chapterNumber);

    long countByMangaId(Long mangaId);

    Optional<Chapter> findTopByMangaIdOrderByChapterNumberDesc(Long mangaId);
}
