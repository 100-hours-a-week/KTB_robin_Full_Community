package ktb3.fullstack.week4.Security.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb3.fullstack.week4.common.error.codes.AuthError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public static final String EMPTY_TOKEN = "Optional.empty";


    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

        try {
            String token = String.valueOf(tokenProvider.resolveAccessTokenFromAuthorization(request));
            String tokenValue = decapsulate(token);

            if(tokenValue.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            if(!tokenProvider.validateAccessToken(tokenValue)) {
                throw new BadCredentialsException("Invalid or expired JWT token");
            }

            // 유효한 토큰이라면 인증객체를 생성 -> SecurityContext 에 저장
            Authentication authentication = tokenProvider.getAuthentication(tokenValue);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            // AuthenticationException 으로 래핑
            throw new BadCredentialsException("Invalid JWT token", e);
        } catch (ServletException | IOException e) {
            // doFilter() 에서 발생가능한 ServletException, IOException 은 Spring MVC 레벨의 GlobalExceptionHanlder 가 처리하도록 위임
            handlerExceptionResolver.resolveException(request, response, null, new ApiException(AuthError.INVALID_ACCESS_TOKEN));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        // 1. Preflight는 JWT 필요 없음
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // 2. 로그인/회원가입/가용성 체크는 JWT 안 봐도 되는 엔드포인트
        if (path.startsWith("/auth/login")
                || path.startsWith("/availability/")) {
            return true;
        }

        if ("POST".equalsIgnoreCase(method) && "/users".equals(path)) {
            return true;
        }

        return false;
    }

    // Optional 로 래핑된 문자열에서 토큰 value 만 추출
    private String decapsulate(String capsulated) {
        if(capsulated.equals(EMPTY_TOKEN)) return "";
        String[] arr = capsulated.split("\\[");
        return arr[1].substring(0, arr[1].length()-1);
    }
}
