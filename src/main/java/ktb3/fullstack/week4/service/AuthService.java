package ktb3.fullstack.week4.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import ktb3.fullstack.week4.auth.JwtTokenProvider;
import ktb3.fullstack.week4.common.error.codes.AuthError;
import ktb3.fullstack.week4.common.error.codes.GenericError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.common.util.CookieUtil;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.auth.LoginResponse;
import ktb3.fullstack.week4.dto.auth.LoginRequest;
import ktb3.fullstack.week4.dto.auth.RefreshResponse;
import ktb3.fullstack.week4.repository.UserRepository;
import ktb3.fullstack.week4.store.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final UserRepository userRepository;

    public LoginResponse login(@Valid LoginRequest dto, HttpServletResponse response) {
        if (dto.getEmail() == null || dto.getPassword() == null) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }

        // 회원가입으로 저장된 실제 사용자 조회 → 실제 atomic id 사용
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ApiException(AuthError.INVALID_EMAIL_OR_PASSWORD));

        // TODO: 이후 BCrypt로 교체. 지금은 단순 비교
        if (!dto.getPassword().equals(user.getPassword())) {
            throw new ApiException(AuthError.INVALID_EMAIL_OR_PASSWORD);
        }

        long userId = user.getId(); // atomic sequence id 를 그대로 사용


        String access = tokenProvider.generateAccessToken(userId);
        String refresh = tokenProvider.generateRefreshToken(userId);

        // refreshToken 저장소 등록
        Instant refreshExpAt = Instant.now().plusSeconds(tokenProvider.getRefreshExpireSeconds());
        refreshTokenStore.save(refresh, userId, refreshExpAt);

        // 쿠키 설정
        CookieUtil.addHttpOnlyCookie(response, REFRESH_COOKIE_NAME, refresh, (int) tokenProvider.getRefreshExpireSeconds());

        return new LoginResponse(
                access,
                "Bearer",
                tokenProvider.getAccessExpireSeconds(),
                tokenProvider.getRefreshExpireSeconds()
        );
    }

    public RefreshResponse refresh(HttpServletRequest request) {
        Optional<String> refreshOpt = tokenProvider.resolveRefreshTokenFromCookies(request, REFRESH_COOKIE_NAME);
        if (refreshOpt.isEmpty()) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }
        String refresh = refreshOpt.get();
        if (!tokenProvider.validateRefreshToken(refresh)) {
            throw new ApiException(AuthError.REFRESH_TOKEN_EXPIRED);
        }
        long userId = tokenProvider.getUserIdFromRefreshToken(refresh)
                .orElseThrow(() -> new ApiException(AuthError.REFRESH_TOKEN_EXPIRED));

        if (!refreshTokenStore.isValid(refresh, userId)) {
            throw new ApiException(AuthError.REFRESH_TOKEN_EXPIRED);
        }

        String newAccess = tokenProvider.generateAccessToken(userId);
        return new RefreshResponse(
                newAccess,
                "Bearer",
                tokenProvider.getAccessExpireSeconds()
        );
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> refreshOpt = tokenProvider.resolveRefreshTokenFromCookies(request, REFRESH_COOKIE_NAME);
        if (refreshOpt.isEmpty()) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }
        String refresh = refreshOpt.get();
        // refresh 검증
        if (!tokenProvider.validateRefreshToken(refresh)) {
            CookieUtil.deleteCookie(response, REFRESH_COOKIE_NAME);
            throw new ApiException(AuthError.REFRESH_TOKEN_EXPIRED);
        }
        long userId = tokenProvider.getUserIdFromRefreshToken(refresh)
                .orElseThrow(() -> new ApiException(AuthError.REFRESH_TOKEN_EXPIRED));

        // 저장소에서 무효화
        refreshTokenStore.invalidate(refresh);

        // 쿠키 삭제
        CookieUtil.deleteCookie(response, REFRESH_COOKIE_NAME);
    }
}
