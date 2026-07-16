package az.manga.demo.manga.page.internal;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chapterId;

    @Column(nullable = false)
    private Integer pageNumber;

    @Column(nullable = false)
    private String imageUrl; // путь к файлу или URL

    private Integer width;
    private Integer height;
}
