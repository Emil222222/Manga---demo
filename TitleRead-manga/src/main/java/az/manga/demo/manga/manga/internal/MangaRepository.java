package az.manga.demo.manga.manga.internal;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface MangaRepository extends JpaRepository<Manga, Long>, JpaSpecificationExecutor<Manga> {

    Optional<Manga> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Modifying
    @Query("UPDATE Manga m SET m.viewCount = m.viewCount + 1 WHERE m.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
