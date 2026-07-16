package az.manga.demo.usercontent.comment.internal;

import az.manga.demo.usercontent.comment.api.dto.CommentDto;
import az.manga.demo.usercontent.comment.internal.dto.CommentCreateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "mangaId", source = "mangaId")
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "userAvatarUrl", ignore = true)
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment toEntity(CommentCreateDto Dto);

}
