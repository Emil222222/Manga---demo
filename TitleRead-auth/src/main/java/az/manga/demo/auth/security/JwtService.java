package az.manga.demo.auth.security;

import az.manga.demo.common.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long accessExpiration;

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(principal.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessExpiration)))
                .claim("authorities", extractAuthorityNames(principal))
                .claim("id", principal.getId())
                .signWith(getSecretKey())
                .compact();
    }

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public Authentication getAuthentication(Claims claims) {
        UserPrincipal principal = extractPrincipal(claims);
        List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    public UserPrincipal extractPrincipal(Claims claims) {
        List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);
        return new UserPrincipal(
                claims.get("id", Long.class),
                claims.getSubject(),
                null,
                false,
                authorities
        );
    }

    private List<String> extractAuthorityNames(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        List<String> authorities = claims.get("authorities", List.class);
        if (authorities == null) return List.of();
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
