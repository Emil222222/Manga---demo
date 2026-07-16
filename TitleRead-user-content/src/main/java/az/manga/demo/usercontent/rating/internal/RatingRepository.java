package az.manga.demo.usercontent.rating.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserIdAndMangaId(Long userId, Long mangaId);

    boolean existsByUserIdAndMangaId(Long userId, Long mangaId);

    void deleteByUserIdAndMangaId(Long userId, Long mangaId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.mangaId = :mangaId")
    Double calculateAverageRating(@Param("mangaId") Long mangaId);

    long countByMangaId(Long mangaId);
}
