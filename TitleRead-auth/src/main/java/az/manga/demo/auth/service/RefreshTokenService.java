package az.manga.demo.auth.service;

import az.manga.demo.common.exception.ErrorMessage;
import az.manga.demo.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-expiration}")
    private  long refreshExpiration;

    private static final String PREFIX = "refresh:";

    public String create(Long userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                PREFIX + token,
                userId.toString(),
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );
        return token;
    }

    public Long validate(String token) {
        String userId = redisTemplate.opsForValue().get(PREFIX + token);
        if (userId == null) {
            throw new UnauthorizedException(ErrorMessage.INVALID_CREDENTIALS);
        }
        return Long.parseLong(userId);
    }

    public String rotate(String oldToken, Long userId) {
        revoke(oldToken);
        return create(userId);
    }

    public void revoke(String token) {
        redisTemplate.delete(PREFIX + token);
    }

}
