package az.manga.demo.manga.page.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDto {
    private Long id;
    private Integer pageNumber;
    private String imageUrl;
    private Integer width;
    private Integer height;
}
