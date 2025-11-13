package az.manga.demo.repository.manga;

import az.manga.demo.model.manga.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Page<Rating> findByMangaId(Long mangaId, Pageable pageable);

    Optional<Rating> findByUserIdAndMangaId(Long userId, Long mangaId);

    boolean existsByUserIdAndMangaId(Long userId, Long mangaId);

    void deleteByUserIdAndMangaId(Long userId, Long mangaId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.manga.id = :mangaId")
    Double calculateAverageRating(@Param("mangaId") Long mangaId);

    long countByMangaId(Long mangaId);
}
