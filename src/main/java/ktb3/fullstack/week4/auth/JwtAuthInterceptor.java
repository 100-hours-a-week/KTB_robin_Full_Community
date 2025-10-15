package ktb3.fullstack.week4.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb3.fullstack.week4.common.error.codes.AuthError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Optional;

public class JwtAuthInterceptor implements HandlerInterceptor {

    public static final String USER_ID = "authenticated_userId";
    private final JwtTokenProvider tokenProvider;
    private final String refreshCookieName;

    public JwtAuthInterceptor(JwtTokenProvider tokenProvider, String refreshCookieName) {
        this.tokenProvider = tokenProvider;
        this.refreshCookieName = refreshCookieName;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Optional<String> tokenOpt = tokenProvider.resolveAccessTokenFromAuthorization(request);
        if (tokenOpt.isEmpty()) {
            throw new ApiException(AuthError.ACCESS_TOKEN_EXPIRED);
        }
        String accessToken = tokenOpt.get();
        if (!tokenProvider.validateAccessToken(accessToken)) {
            throw new ApiException(AuthError.ACCESS_TOKEN_EXPIRED);
        }
        long userId = tokenProvider.getUserIdFromAccessToken(accessToken)
                .orElseThrow(() ->
                        new ApiException(AuthError.ACCESS_TOKEN_EXPIRED)
                );
        request.setAttribute(USER_ID, userId);
        return true;
    }
}
