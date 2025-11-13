package az.manga.demo.service.manga;

import az.manga.demo.model.manga.Manga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MangaService {
    Page<Manga> findAllByOrderByNameAsc(Pageable pageable);
}
