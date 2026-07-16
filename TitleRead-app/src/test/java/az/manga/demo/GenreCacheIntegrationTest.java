package az.manga.demo;

import az.manga.demo.manga.catalog.api.GenreService;
import az.manga.demo.manga.catalog.api.dto.GenreDto;
import az.manga.demo.manga.catalog.internal.dto.GenreCreateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.SimpleKey;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GenreCacheIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private GenreService genreService;

    @Test
    @DisplayName("getById кэширует результат")
    void shouldCacheGenreById() {
        GenreCreateDto createDto = new GenreCreateDto("Action", "action", null);
        GenreDto created = genreService.create(createDto);

        genreService.getById(created.getId());

        Cache cache = cacheManager.getCache("genres");
        assertThat(cache).isNotNull();
        assertThat(cache.get(created.getId(), GenreDto.class)).isNotNull();
    }

    @Test
    @DisplayName("getAll кэширует список жанров")
    void shouldCacheAllGenres() {
        genreService.create(new GenreCreateDto("Comedy", "comedy", null));

        List<GenreDto> first = genreService.getAll();

        List<GenreDto> second = genreService.getAll();

        assertThat(first).isEqualTo(second);

        Cache cache = cacheManager.getCache("genres:all");
        assertThat(cache).isNotNull();
        assertThat(cache.get(SimpleKey.EMPTY)).isNotNull();
    }

    @Test
    @DisplayName("create инвалидирует кэш genres")
    void shouldEvictCacheOnCreate() {
        genreService.create(new GenreCreateDto("Drama", "drama", null));
        genreService.getAll();

        Cache cache = cacheManager.getCache("genres:all");
        assertThat(cache).isNotNull();
        Cache.ValueWrapper value = cache.get(SimpleKey.EMPTY);
        assertThat(value).isNotNull();

        genreService.create(new GenreCreateDto("Horror", "horror", null));

        Cache.ValueWrapper valueAfterEvict = cache.get(SimpleKey.EMPTY);
        assertThat(valueAfterEvict).isNull();
    }

    @Test
    @DisplayName("delete инвалидирует кэш genres")
    void shouldEvictCacheOnDelete() {
        GenreDto created = genreService.create(new GenreCreateDto("Thriller", "thriller", null));
        genreService.getAll();

        Cache cache = cacheManager.getCache("genres:all");
        assertThat(cache).isNotNull();
        Cache.ValueWrapper value = cache.get(SimpleKey.EMPTY);
        assertThat(value).isNotNull();

        genreService.delete(created.getId());

        Cache.ValueWrapper valueAfterEvict = cache.get(SimpleKey.EMPTY);
        assertThat(valueAfterEvict).isNull();
    }

    @Test
    @DisplayName("update инвалидирует кэш genres")
    void shouldEvictCacheOnUpdate() {
        GenreDto created = genreService.create(new GenreCreateDto("Mystery", "mystery", null));
        genreService.getAll();
        Cache cache = cacheManager.getCache("genres:all");
        assertThat(cache).isNotNull();
        Cache.ValueWrapper value = cache.get(SimpleKey.EMPTY);
        assertThat(value).isNotNull();

        genreService.update(created.getId(), new GenreCreateDto("Mystery Updated", "mystery", null));

        Cache.ValueWrapper valueAfterEvict = cache.get(SimpleKey.EMPTY);
        assertThat(valueAfterEvict).isNull();
    }
}
