package az.manga.demo.manga.catalog.internal;

import az.manga.demo.manga.catalog.api.TagService;
import az.manga.demo.manga.catalog.api.dto.TagDto;
import az.manga.demo.manga.catalog.internal.dto.TagCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tag")
public class TagController {
    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDto>> getAll() {
        return ResponseEntity.ok(tagService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tagService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagDto> createTag(@RequestBody TagCreateDto tagDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.create(tagDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagDto> updateTag(
            @PathVariable Long id,
            @RequestBody TagCreateDto tagDto) {
        return ResponseEntity.ok(tagService.update(id, tagDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagDto> deleteTag(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
