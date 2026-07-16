package az.manga.demo.manga.chapter.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    Page<Chapter> findByMangaIdOrderByChapterNumberAsc(Long mangaId, Pageable pageable);

    Optional<Chapter> findByMangaIdAndChapterNumber(Long mangaId, Double chapterNumber);

    long countByMangaId(Long mangaId);

    Optional<Chapter> findTopByMangaIdOrderByChapterNumberDesc(Long mangaId);
}
