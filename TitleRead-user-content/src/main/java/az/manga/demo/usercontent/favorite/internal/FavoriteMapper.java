package az.manga.demo.usercontent.favorite.internal;

import az.manga.demo.usercontent.favorite.api.dto.FavoriteDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FavoriteMapper {

    @Mapping(target = "mangaListDto", ignore = true)
    FavoriteDto toDto(UserFavorite favorite);
}
