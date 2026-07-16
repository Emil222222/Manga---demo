package az.manga.demo.manga.chapter.internal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long mangaId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Double chapterNumber;

    private Integer volumeNumber;

    @Builder.Default
    private Integer pageCount = 0;

    @CreatedDate
    private LocalDateTime publishedAt;
}
