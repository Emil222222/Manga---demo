package az.manga.demo.manga.service;

import az.manga.demo.common.exception.ConflictException;
import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.manga.catalog.api.dto.TagDto;
import az.manga.demo.manga.catalog.internal.Tag;
import az.manga.demo.manga.catalog.internal.TagMapper;
import az.manga.demo.manga.catalog.internal.TagRepository;
import az.manga.demo.manga.catalog.internal.TagServiceImpl;
import az.manga.demo.manga.catalog.internal.dto.TagCreateDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock private TagRepository tagRepository;
    @Mock private TagMapper tagMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("создаёт тег если slug уникален")
        void shouldCreateTag() {
            TagCreateDto dto = new TagCreateDto("Isekai", "isekai");
            Tag entity = new Tag();
            Tag saved = new Tag();
            TagDto expectedDto = new TagDto(1L, "Isekai", "isekai");

            Mockito.when(tagRepository.existsBySlug(dto.getSlug())).thenReturn(false);
            Mockito.when(tagMapper.toEntity(dto)).thenReturn(entity);
            Mockito.when(tagRepository.save(entity)).thenReturn(saved);
            Mockito.when(tagMapper.toDto(saved)).thenReturn(expectedDto);

            TagDto result = tagService.create(dto);

            assertThat(result).isEqualTo(expectedDto);
            Mockito.verify(tagRepository).save(entity);
        }

        @Test
        @DisplayName("выбрасывает ConflictException если slug уже существует")
        void shouldThrowConflictIfSlugExists() {
            TagCreateDto dto = new TagCreateDto("Isekai", "isekai");
            Mockito.when(tagRepository.existsBySlug(dto.getSlug())).thenReturn(true);

            Assertions.assertThatThrownBy(() -> tagService.create(dto))
                    .isInstanceOf(ConflictException.class);

            Mockito.verify(tagRepository, Mockito.never()).save(ArgumentMatchers.any());
            Mockito.verifyNoInteractions(tagMapper);
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("возвращает тег по id")
        void shouldReturnTagById() {
            Tag tag = new Tag();
            TagDto expectedDto = new TagDto(1L, "Isekai", "isekai");

            Mockito.when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
            Mockito.when(tagMapper.toDto(tag)).thenReturn(expectedDto);

            TagDto result = tagService.getById(1L);

            assertThat(result).isEqualTo(expectedDto);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если тег не найден")
        void shouldThrowNotFoundIfTagNotFound() {
            Mockito.when(tagRepository.findById(99L)).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> tagService.getById(99L))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verifyNoInteractions(tagMapper);
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("возвращает список всех тегов")
        void shouldReturnAllTags() {
            Tag t1 = new Tag();
            Tag t2 = new Tag();
            TagDto dto1 = new TagDto(1L, "Isekai", "isekai");
            TagDto dto2 = new TagDto(2L, "Shounen", "shounen");

            Mockito.when(tagRepository.findAllByOrderByNameAsc()).thenReturn(List.of(t1, t2));
            Mockito.when(tagMapper.toDto(t1)).thenReturn(dto1);
            Mockito.when(tagMapper.toDto(t2)).thenReturn(dto2);

            List<TagDto> result = tagService.getAll();

            Assertions.assertThat(result).containsExactly(dto1, dto2);
        }

        @Test
        @DisplayName("возвращает пустой список если тегов нет")
        void shouldReturnEmptyListIfNoTags() {
            Mockito.when(tagRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

            List<TagDto> result = tagService.getAll();

            Assertions.assertThat(result).isEmpty();
            Mockito.verifyNoInteractions(tagMapper);
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("обновляет тег")
        void shouldUpdateTag() {
            Long id = 1L;
            TagCreateDto dto = new TagCreateDto("Shoujo", "shoujo");
            Tag tag = new Tag();
            Tag saved = new Tag();
            TagDto expectedDto = new TagDto(id, "Shoujo", "shoujo");

            Mockito.when(tagRepository.findById(id)).thenReturn(Optional.of(tag));
            Mockito.doNothing().when(tagMapper).updateEntity(tag, dto);
            Mockito.when(tagRepository.save(tag)).thenReturn(saved);
            Mockito.when(tagMapper.toDto(saved)).thenReturn(expectedDto);

            TagDto result = tagService.update(id, dto);

            assertThat(result).isEqualTo(expectedDto);
            Mockito.verify(tagMapper).updateEntity(tag, dto);
            Mockito.verify(tagRepository).save(tag);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если тег не найден")
        void shouldThrowNotFoundIfTagNotFound() {
            Mockito.when(tagRepository.findById(99L)).thenReturn(Optional.empty());

            Assertions.assertThatThrownBy(() -> tagService.update(99L, new TagCreateDto()))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verify(tagRepository, Mockito.never()).save(ArgumentMatchers.any());
            Mockito.verifyNoInteractions(tagMapper);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("удаляет тег если существует")
        void shouldDeleteTag() {
            Mockito.when(tagRepository.existsById(1L)).thenReturn(true);

            tagService.delete(1L);

            Mockito.verify(tagRepository).deleteById(1L);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если тег не найден")
        void shouldThrowNotFoundIfTagNotFound() {
            Mockito.when(tagRepository.existsById(99L)).thenReturn(false);

            Assertions.assertThatThrownBy(() -> tagService.delete(99L))
                    .isInstanceOf(NotFoundException.class);

            Mockito.verify(tagRepository, Mockito.never()).deleteById(ArgumentMatchers.any());
        }
    }
}
