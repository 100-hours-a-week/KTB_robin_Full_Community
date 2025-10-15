package ktb3.fullstack.week4.store;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class RefreshTokenStore {

    @AllArgsConstructor
    private static class RefreshToken {
        long userId;
        Instant expiresAt;
    }

    private final Map<String, RefreshToken> refreshTokenStore = new ConcurrentHashMap<>();

    // 토큰 저장
    public void save(String refreshToken, long userId, Instant expiresAt) {
        refreshTokenStore.put(refreshToken, new RefreshToken(userId, expiresAt));
    }

    public boolean isValid(String refreshToken, long userId) {
        RefreshToken e = refreshTokenStore.get(refreshToken);
        if (e == null) return false;
        if (e.userId != userId) return false;
        if (e.expiresAt.isBefore(Instant.now())) {
            refreshTokenStore.remove(refreshToken);
            return false;
        }
        return true;
    }

    // 토큰 무효화
    public void invalidate(String refreshToken) {
        refreshTokenStore.remove(refreshToken);
    }
}
