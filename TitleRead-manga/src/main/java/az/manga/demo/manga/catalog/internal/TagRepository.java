package az.manga.demo.manga.catalog.internal;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findAllByOrderByNameAsc();

    boolean existsBySlug(@NotBlank(message = "Genre slug is required") String slug);
}
