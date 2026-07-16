package az.manga.demo.usercontent.comment.internal;

import az.manga.demo.usercontent.comment.api.CommentService;
import az.manga.demo.usercontent.comment.api.dto.CommentDto;
import az.manga.demo.usercontent.comment.internal.dto.CommentCreateDto;
import az.manga.demo.usercontent.comment.internal.dto.CommentUpdateDto;
import az.manga.demo.common.exception.ErrorMessage;
import az.manga.demo.common.exception.ForbiddenException;
import az.manga.demo.common.exception.NotFoundException;
import az.manga.demo.user.api.UserService;
import az.manga.demo.user.api.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;


    @Override
    @CacheEvict(value = "comments:count", key = "#dto.mangaId")
    public CommentDto create(Long userId, CommentCreateDto dto) {
        log.info("Creating comment for manga id = {} by user id = {}",dto.getMangaId(), userId);
        Comment comment = commentMapper.toEntity(dto);
        comment.setUserId(userId);
        return enrichWithUser(commentMapper.toDto(commentRepository.save(comment)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDto> getByManga(Long mangaId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByMangaIdOrderByCreatedAtDesc(mangaId, pageable);

        Set<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());

        Map<Long, UserDto> userMap = userService.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(UserDto::getId, user -> user));

        return comments.map(comment ->{
            CommentDto dto = commentMapper.toDto(comment);
            UserDto userDto = userMap.get(comment.getUserId());
            if (userDto != null) {
                dto.setUsername(userDto.getUsername());
                dto.setUserAvatarUrl(userDto.getAvatarUrl());
            }
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDto> getByUser(Long userId, Pageable pageable) {
        UserDto user = userService.getProfile(userId); // один запрос

        return commentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(comment -> {
                    CommentDto dto = commentMapper.toDto(comment);
                    dto.setUsername(user.getUsername());
                    dto.setUserAvatarUrl(user.getAvatarUrl());
                    return dto;
                });
    }

    @Override
    public CommentDto update(Long commentId, Long userId, CommentUpdateDto dto) {
        log.info("Updating comment id = {} by user id = {}", commentId, userId);
        Comment comment = findById(commentId);
        if (!comment.getUserId().equals(userId)) {
            throw new ForbiddenException(ErrorMessage.ACCESS_DENIED);
        }
        comment.setContent(dto.getContent());
        return enrichWithUser(commentMapper.toDto(commentRepository.save(comment)));
    }

    @Override
    @CacheEvict(value = "comments:count", key = "#mangaId")
    public void delete(Long commentId, Long mangaId, Long userId) {
        log.info("Deleting comment id = {} by user id = {} in manga id = {}", commentId, userId, mangaId);
        Comment comment = findById(commentId);
        if (!comment.getUserId().equals(userId) || !comment.getMangaId().equals(mangaId)) {
            throw new ForbiddenException(ErrorMessage.ACCESS_DENIED);
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    @Cacheable(value = "comments:count", key = "#mangaId")
    @Transactional(readOnly = true)
    public long countByManga(Long mangaId) {
        return commentRepository.countByMangaId(mangaId);
    }

    private Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(()-> new NotFoundException(ErrorMessage.COMMENT_NOT_FOUND));
    }

    private CommentDto enrichWithUser(CommentDto dto) {
        UserDto user = userService.getProfile(dto.getUserId());
        dto.setUsername(user.getUsername());
        dto.setUserAvatarUrl(user.getAvatarUrl());
        return dto;
    }


}
