package az.manga.demo.manga.page.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageRepository  extends JpaRepository<Page, Long> {

    List<Page> findByChapterIdOrderByPageNumberAsc(Long chapterId);

}
