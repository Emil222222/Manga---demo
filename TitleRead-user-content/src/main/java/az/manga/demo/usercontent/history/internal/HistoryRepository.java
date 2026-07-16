package az.manga.demo.usercontent.history.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoryRepository extends JpaRepository<ReadingHistory,Long> {

    Page<ReadingHistory> findByUserIdOrderByLastReadAtDesc(Long userId, Pageable pageable);

    Optional<ReadingHistory> findByUserIdAndMangaId(Long userId, Long mangaId);
}
