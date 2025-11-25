package ktb3.fullstack.week4.Security.jwt;


import lombok.Getter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Getter
@Component
public class JwtTokenProvider {

    private static final String ACCESS_SECRET_BASE64 = "MzQ2N2I2MjM2YzU2YzQzYjY5MzY5NzNhMzQ2N2I2MjM2YzU2YzQzYjY5MzY5NzNh"; // 예시 base64
    private static final String REFRESH_SECRET_BASE64 = "N2M2MzYzNDM2YzU2MzQzYjY5MzY5NzNhN2M2MzYzNDM2YzU2MzQzYjY5MzY5NzNh"; // 예시 base64

    // 유효기간(초)
    private static final long ACCESS_EXPIRE_SECONDS = 60L * 60L;         // 1시간
    private static final long REFRESH_EXPIRE_SECONDS = 60L * 60L * 24L * 7L; // 7일

    private final Key accessKey;
    private final Key refreshKey;

    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(UserDetailsService userDetailsService) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(ACCESS_SECRET_BASE64));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(REFRESH_SECRET_BASE64));
        this.userDetailsService = userDetailsService;
    }

    public Authentication getAuthentication(String token) {
        Long userId = getUserIdFromAccessToken(token).
                orElseThrow(() -> new JwtException("토큰에 회원 id가 없습니다."));

        UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String generateAccessToken(long userId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ACCESS_EXPIRE_SECONDS);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(long userId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(REFRESH_EXPIRE_SECONDS);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        return validate(token, accessKey);
    }

    public boolean validateRefreshToken(String token) {
        return validate(token, refreshKey);
    }

    private boolean validate(String token, Key key) {
        try {
            Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Optional<Long> getUserIdFromAccessToken(String token) {
        return getSubject(token, accessKey);
    }

    public Optional<Long> getUserIdFromRefreshToken(String token) {
        return getSubject(token, refreshKey);
    }

    private Optional<Long> getSubject(String token, Key key) {
        try {
            Claims claims = Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token).getPayload();
            return Optional.of(Long.parseLong(claims.getSubject()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public long getAccessExpireSeconds() {
        return ACCESS_EXPIRE_SECONDS;
    }

    public long getRefreshExpireSeconds() {
        return REFRESH_EXPIRE_SECONDS;
    }

    public Optional<String> resolveAccessTokenFromAuthorization(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) return Optional.empty();
        if (!header.startsWith("Bearer ")) return Optional.empty();
        String token = header.substring(7).trim();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }

    public Optional<String> resolveRefreshTokenFromCookies(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        for (Cookie c : cookies) {
            if (cookieName.equals(c.getName())) {
                return Optional.ofNullable(c.getValue());
            }
        }
        return Optional.empty();
    }
}
