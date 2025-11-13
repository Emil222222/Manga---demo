package az.manga.demo.repository.user;

import az.manga.demo.model.user.ReadingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory,Long> {
    // История чтения - с пагинацией!
    Page<ReadingHistory> findByUserIdOrderByLastReadAtDesc(Long userId, Pageable pageable);

    Optional<ReadingHistory> findByUserIdAndMangaId(Long userId, Long mangaId);

    boolean existsByUserIdAndMangaId(Long userId, Long mangaId);
}
