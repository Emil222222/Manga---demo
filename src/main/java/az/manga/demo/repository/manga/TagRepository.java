package az.manga.demo.repository.manga;

import az.manga.demo.model.manga.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Page<Tag> findAllByOrderByNameAsc(Pageable pageable);

    List<Tag> findAllByOrderByNameAsc();
}
