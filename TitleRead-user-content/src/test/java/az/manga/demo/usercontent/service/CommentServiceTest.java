package az.manga.demo.usercontent.service;

import az.manga.demo.common.exception.ForbiddenException;
import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.user.api.UserService;
import az.manga.demo.user.api.dto.UserDto;
import az.manga.demo.usercontent.comment.api.dto.CommentDto;
import az.manga.demo.usercontent.comment.internal.Comment;
import az.manga.demo.usercontent.comment.internal.CommentMapper;
import az.manga.demo.usercontent.comment.internal.CommentRepository;
import az.manga.demo.usercontent.comment.internal.CommentServiceImpl;
import az.manga.demo.usercontent.comment.internal.dto.CommentCreateDto;
import az.manga.demo.usercontent.comment.internal.dto.CommentUpdateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("создаёт комментарий и обогащает данными пользователя")
        void shouldCreateComment() {
            Long userId = 1L;
            CommentCreateDto dto = new CommentCreateDto(10L, "Great manga!");
            Comment comment = Comment.builder().mangaId(10L).content("Great manga!").build();
            Comment saved = Comment.builder().id(1L).userId(userId).mangaId(10L).content("Great manga!").build();
            CommentDto commentDto = new CommentDto();
            commentDto.setId(1L);
            commentDto.setUserId(userId);
            UserDto userDto = UserDto.builder().id(userId).username("Emil").avatarUrl("url").build();

            when(commentMapper.toEntity(dto)).thenReturn(comment);
            when(commentRepository.save(comment)).thenReturn(saved);
            when(commentMapper.toDto(saved)).thenReturn(commentDto);
            when(userService.getProfile(userId)).thenReturn(userDto);

            CommentDto result = commentService.create(userId, dto);

            assertThat(result).isEqualTo(commentDto);
            assertThat(result.getUsername()).isEqualTo("Emil");
            assertThat(result.getUserAvatarUrl()).isEqualTo("url");
            assertThat(comment.getUserId()).isEqualTo(userId);

            verify(commentMapper).toEntity(dto);
            verify(commentMapper).toDto(saved);
            verify(userService).getProfile(userId);
            verify(commentRepository).save(comment);
        }

        @Test
        @DisplayName("выбрасывает исключение если пользователь не найден")
        void shouldThrowIfUserNotFound() {
            Long userId = 99L;
            CommentCreateDto dto = new CommentCreateDto(10L, "text");
            Comment comment = Comment.builder().mangaId(10L).build();
            Comment saved = Comment.builder().id(1L).build();
            CommentDto commentDto = new CommentDto();
            commentDto.setUserId(userId);

            when(commentMapper.toEntity(dto)).thenReturn(comment);
            when(commentRepository.save(comment)).thenReturn(saved);
            when(commentMapper.toDto(saved)).thenReturn(commentDto);
            when(userService.getProfile(userId)).thenThrow(NotFoundException.class);

            assertThatThrownBy(() -> commentService.create(userId, dto))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByManga()")
    class GetByManga {

        @Test
        @DisplayName("возвращает комментарии с данными пользователей")
        void shouldReturnCommentsWithUserData() {
            Long mangaId = 10L;
            Pageable pageable = PageRequest.of(0, 10);
            Long userId = 1L;

            Comment comment = Comment.builder().id(1L).userId(userId).mangaId(mangaId).content("Nice!").build();
            Page<Comment> commentPage = new PageImpl<>(List.of(comment));
            CommentDto commentDto = new CommentDto();
            commentDto.setUserId(userId);
            UserDto userDto = UserDto.builder().id(userId).username("Emil").avatarUrl("url").build();

            when(commentRepository.findByMangaIdOrderByCreatedAtDesc(mangaId, pageable))
                    .thenReturn(commentPage);
            when(userService.getUsersByIds(Set.of(userId))).thenReturn(List.of(userDto));
            when(commentMapper.toDto(comment)).thenReturn(commentDto);

            Page<CommentDto> result = commentService.getByManga(mangaId, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst()).isEqualTo(commentDto);
            assertThat(commentDto.getUsername()).isEqualTo("Emil");
            assertThat(commentDto.getUserAvatarUrl()).isEqualTo("url");

            verify(commentMapper).toDto(comment);
            verify(userService).getUsersByIds(Set.of(userId));
            verify(userService, times(1)).getUsersByIds(any());
        }

        @Test
        @DisplayName("корректно обогащает несколько комментариев данными пользователей")
        void shouldEnrichMultipleComments() {
            Long mangaId = 10L;
            Pageable pageable = PageRequest.of(0, 10);

            Comment comment1 = Comment.builder().id(1L).userId(1L).mangaId(mangaId).content("Nice!").build();
            Comment comment2 = Comment.builder().id(2L).userId(2L).mangaId(mangaId).content("Great!").build();

            CommentDto dto1 = new CommentDto();
            dto1.setUserId(1L);
            CommentDto dto2 = new CommentDto();
            dto2.setUserId(2L);

            UserDto user1 = UserDto.builder().id(1L).username("Emil").avatarUrl("url1").build();
            UserDto user2 = UserDto.builder().id(2L).username("John").avatarUrl("url2").build();

            when(commentRepository.findByMangaIdOrderByCreatedAtDesc(mangaId, pageable))
                    .thenReturn(new PageImpl<>(List.of(comment1, comment2)));
            when(userService.getUsersByIds(Set.of(1L, 2L))).thenReturn(List.of(user1, user2));
            when(commentMapper.toDto(comment1)).thenReturn(dto1);
            when(commentMapper.toDto(comment2)).thenReturn(dto2);

            commentService.getByManga(mangaId, pageable);

            assertThat(dto1.getUsername()).isEqualTo("Emil");
            assertThat(dto1.getUserAvatarUrl()).isEqualTo("url1");
            assertThat(dto2.getUsername()).isEqualTo("John");
            assertThat(dto2.getUserAvatarUrl()).isEqualTo("url2");
            verify(userService, times(1)).getUsersByIds(any());
        }

        @Test
        @DisplayName("не устанавливает username если пользователь не найден в map")
        void shouldNotSetUsernameIfUserNotFound() {
            Long mangaId = 10L;
            Pageable pageable = PageRequest.of(0, 10);

            Comment comment = Comment.builder().id(1L).userId(99L).mangaId(mangaId).content("Nice!").build();
            CommentDto dto = new CommentDto();
            dto.setUserId(99L);

            when(commentRepository.findByMangaIdOrderByCreatedAtDesc(mangaId, pageable))
                    .thenReturn(new PageImpl<>(List.of(comment)));
            when(userService.getUsersByIds(Set.of(99L))).thenReturn(List.of());
            when(commentMapper.toDto(comment)).thenReturn(dto);

            commentService.getByManga(mangaId, pageable);

            assertThat(dto.getUsername()).isNull();
            assertThat(dto.getUserAvatarUrl()).isNull();
        }

        @Test
        @DisplayName("возвращает пустую страницу если комментариев нет")
        void shouldReturnEmptyPageIfNoComments() {
            Long mangaId = 10L;
            Pageable pageable = PageRequest.of(0, 10);

            when(commentRepository.findByMangaIdOrderByCreatedAtDesc(mangaId, pageable))
                    .thenReturn(Page.empty());
            when(userService.getUsersByIds(Set.of())).thenReturn(List.of());

            Page<CommentDto> result = commentService.getByManga(mangaId, pageable);

            assertThat(result.getContent()).isEmpty();

            verifyNoInteractions(commentMapper);
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("обновляет комментарий владельца")
        void shouldUpdateComment() {
            Long commentId = 1L;
            Long userId = 1L;
            CommentUpdateDto updateDto = new CommentUpdateDto("Updated content");
            Comment comment = Comment.builder().id(commentId).userId(userId).mangaId(10L).content("Old content").build();
            Comment saved = Comment.builder().id(commentId).userId(userId).mangaId(10L).content("Updated content").build();
            CommentDto commentDto = new CommentDto();
            commentDto.setUserId(userId);
            UserDto userDto = UserDto.builder().id(userId).username("Emil").build();

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
            when(commentRepository.save(comment)).thenReturn(saved);
            when(commentMapper.toDto(saved)).thenReturn(commentDto);
            when(userService.getProfile(userId)).thenReturn(userDto);

            CommentDto result = commentService.update(commentId, userId, updateDto);

            assertThat(comment.getContent()).isEqualTo("Updated content");
            assertThat(result).isEqualTo(commentDto);

            verify(commentRepository).save(comment);
            verify(commentMapper).toDto(saved);
            verify(userService).getProfile(userId);
        }

        @Test
        @DisplayName("выбрасывает ForbiddenException если не владелец")
        void shouldThrowForbiddenIfNotOwner() {
            Long commentId = 1L;
            Long userId = 1L;
            Long otherUserId = 2L;
            CommentUpdateDto updateDto = new CommentUpdateDto("Updated");
            Comment comment = Comment.builder().id(commentId).userId(otherUserId).build();

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.update(commentId, userId, updateDto))
                    .isInstanceOf(ForbiddenException.class);

            verify(commentRepository, never()).save(any());
            verifyNoInteractions(commentMapper);
            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если комментарий не найден")
        void shouldThrowNotFoundIfCommentNotFound() {
            Long commentId = 99L;
            Long userId = 1L;

            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.update(commentId, userId, new CommentUpdateDto("x")))
                    .isInstanceOf(NotFoundException.class);

            verify(commentRepository, never()).save(any());
            verifyNoInteractions(commentMapper);
            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("удаляет комментарий владельца")
        void shouldDeleteComment() {
            Long commentId = 1L;
            Long mangaId = 10L;
            Long userId = 1L;
            Comment comment = Comment.builder().id(commentId).userId(userId).mangaId(mangaId).build();

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            commentService.delete(commentId, mangaId, userId);

            verify(commentRepository).deleteById(commentId);
        }

        @Test
        @DisplayName("выбрасывает ForbiddenException если не владелец")
        void shouldThrowForbiddenIfNotOwner() {
            Long commentId = 1L;
            Long mangaId = 10L;
            Long userId = 1L;
            Long otherUserId = 2L;
            Comment comment = Comment.builder().id(commentId).userId(otherUserId).mangaId(mangaId).build();

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.delete(commentId, mangaId, userId))
                    .isInstanceOf(ForbiddenException.class);

            verify(commentRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("выбрасывает ForbiddenException если mangaId не совпадает")
        void shouldThrowForbiddenIfWrongMangaId() {
            Long commentId = 1L;
            Long mangaId = 10L;
            Long wrongMangaId = 99L;
            Long userId = 1L;
            Comment comment = Comment.builder().id(commentId).userId(userId).mangaId(mangaId).build();

            when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.delete(commentId, wrongMangaId, userId))
                    .isInstanceOf(ForbiddenException.class);

            verify(commentRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("выбрасывает NotFoundException если комментарий не найден")
        void shouldThrowNotFoundIfCommentNotFound() {
            Long commentId = 99L;

            when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.delete(commentId, 1L, 1L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("countByManga()")
    class CountByManga {

        @Test
        @DisplayName("возвращает количество комментариев")
        void shouldReturnCommentCount() {
            Long mangaId = 10L;
            when(commentRepository.countByMangaId(mangaId)).thenReturn(5L);

            long result = commentService.countByManga(mangaId);

            assertThat(result).isEqualTo(5L);

            verify(commentRepository).countByMangaId(mangaId);
        }

        @Test
        @DisplayName("возвращает 0 если комментариев нет")
        void shouldReturnZeroIfNoComments() {
            Long mangaId = 10L;
            when(commentRepository.countByMangaId(mangaId)).thenReturn(0L);

            long result = commentService.countByManga(mangaId);

            assertThat(result).isZero();

            verify(commentRepository).countByMangaId(mangaId);
        }
    }
}
