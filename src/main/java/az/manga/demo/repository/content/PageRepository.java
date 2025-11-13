package az.manga.demo.repository.content;

import az.manga.demo.model.content.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageRepository  extends JpaRepository<Page, Long> {
    List<Page> findByChapterId(Long chapterId);

    // Страницы отсортированные по номеру
    List<Page> findByChapterIdOrderByPageNumberAsc(Long chapterId);

    // Количество страниц в главе
    long countByChapterId(Long chapterId);
}
