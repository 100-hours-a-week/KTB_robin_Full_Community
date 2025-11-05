package ktb3.fullstack.week4.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import ktb3.fullstack.week4.auth.JwtTokenProvider;
import ktb3.fullstack.week4.common.error.codes.AuthError;
import ktb3.fullstack.week4.common.error.codes.GenericError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.common.security.PasswordHasher;
import ktb3.fullstack.week4.common.util.CookieUtil;
import ktb3.fullstack.week4.domain.auth.RefreshToken;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.auth.LoginResponse;
import ktb3.fullstack.week4.dto.auth.LoginRequest;
import ktb3.fullstack.week4.dto.auth.RefreshResponse;
import ktb3.fullstack.week4.repository.auth.RefreshTokenRepository;
import ktb3.fullstack.week4.repository.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Transactional
    public LoginResponse login(@Valid LoginRequest dto, HttpServletResponse response) {
        if (dto.getEmail() == null || dto.getPassword() == null) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }

        // 등록된 이메일 - 사용자 간 적절성 검사
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ApiException(AuthError.INVALID_EMAIL_OR_PASSWORD));

        // 비밀번호 유효성 검사
        if (!passwordHasher.matches(dto.getPassword(), user.getHashedPassword())) {
            throw new ApiException(AuthError.INVALID_EMAIL_OR_PASSWORD);
        }

        long userId = user.getId();


        // 액세스, 리프레시 토큰 발급
        String access = tokenProvider.generateAccessToken(userId);

        // 데이터베이스에 이미 존재하는 사용자의 refresh 를 삭제하고 새로만들기
        refreshTokenRepository.deleteAllByUserId(userId);
        String refresh = tokenProvider.generateRefreshToken(userId);

        // refreshToken 저장소 등록
        LocalDateTime refreshExpAt = LocalDateTime.now().plusSeconds(tokenProvider.getRefreshExpireSeconds());
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(userId)
                .tokenString(refresh)
                .expiresAt(refreshExpAt)
                .build()
        );

        // 쿠키 설정
        CookieUtil.addHttpOnlyCookie(response, REFRESH_COOKIE_NAME, refresh, (int) tokenProvider.getRefreshExpireSeconds());

        return new LoginResponse(
                access,
                "Bearer",
                tokenProvider.getAccessExpireSeconds(),
                tokenProvider.getRefreshExpireSeconds()
        );
    }

    @Transactional
    public RefreshResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> refreshOpt = tokenProvider.resolveRefreshTokenFromCookies(request, REFRESH_COOKIE_NAME);

        if (refreshOpt.isEmpty()) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }

        String refresh = refreshOpt.get();
        if (!tokenProvider.validateRefreshToken(refresh)) {
            throw new ApiException(AuthError.REFRESH_TOKEN_VALIDATION_FAIL);
        }

        long userId = tokenProvider.getUserIdFromRefreshToken(refresh)
                .orElseThrow(() -> new ApiException(AuthError.REFRESH_TOKEN_EXPIRED));

        RefreshToken token = refreshTokenRepository.findByUserId(userId);
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {  // 리프레시 토큰 만료
            refreshTokenRepository.delete(token); // 기존 리프레시 토큰 삭제

            // 리프레시 토큰 재발급
            String newToken = tokenProvider.generateRefreshToken(userId);

            // 리프레시 토큰 저장
            LocalDateTime refreshExpAt = LocalDateTime.now().plusSeconds(tokenProvider.getRefreshExpireSeconds());
            refreshTokenRepository.save(RefreshToken.builder()
                    .userId(userId)
                    .tokenString(newToken)
                    .expiresAt(refreshExpAt)
                    .build()
            );

            // 기존 리프레시 토큰 쿠키 삭제
            CookieUtil.deleteCookie(response, REFRESH_COOKIE_NAME);
            // 새로 발급한 리프레시 토큰 쿠키에 등록
            CookieUtil.addHttpOnlyCookie(response, REFRESH_COOKIE_NAME, newToken, (int) tokenProvider.getRefreshExpireSeconds());
        }

        String newAccess = tokenProvider.generateAccessToken(userId);
        return new RefreshResponse(
                newAccess,
                "Bearer",
                tokenProvider.getAccessExpireSeconds()
        );
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        Optional<String> refreshOpt = tokenProvider.resolveRefreshTokenFromCookies(request, REFRESH_COOKIE_NAME);

        if (refreshOpt.isEmpty()) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }

        String refresh = refreshOpt.get();

        // 리프레시 토큰 검증
        if (!tokenProvider.validateRefreshToken(refresh)) {
            CookieUtil.deleteCookie(response, REFRESH_COOKIE_NAME);
            throw new ApiException(AuthError.REFRESH_TOKEN_EXPIRED);
        }

        long userId = tokenProvider.getUserIdFromRefreshToken(refresh)
                .orElseThrow(() -> new ApiException(AuthError.REFRESH_TOKEN_EXPIRED));

        RefreshToken token = refreshTokenRepository.findByUserId(userId);

        // 저장소에서 무효화
        refreshTokenRepository.delete(token);

        // 쿠키 삭제
        CookieUtil.deleteCookie(response, REFRESH_COOKIE_NAME);
    }
}
